package com.ai.classification.service;

import com.ai.classification.entity.AlterWord;
import com.ai.classification.entity.Exclusion;
import com.ai.classification.entity.FirstPref;
import com.ai.classification.entity.LastPref;
import com.ai.classification.entity.SecondPref;
import com.ai.classification.entity.ThirdPref;
import com.ai.classification.repository.AlterWordRepository;
import com.ai.classification.repository.ExclusionRepository;
import com.ai.classification.repository.FirstPrefRepository;
import com.ai.classification.repository.LastPrefRepository;
import com.ai.classification.repository.SecondPrefRepository;
import com.ai.classification.repository.ThirdPrefRepository;
import com.ai.classification.util.TextInflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java 1.8 port of the merged material-description classification engine
 * (class_run_batch.py / optimized_code.py / complete_class.py).
 *
 * The reference lookup tables (Third_Pref, Second_Pref, First_Pref(NEW),
 * Last_Pref, alter words, exclusions) are loaded from H2 via JPA at startup
 * by ExcelDataLoader and cached in memory here, mirroring the Pandas
 * DataFrames the Python script kept in memory (class_check, check1,
 * r1_check, lst_check, obj_check, alter_df, exmp_df).
 */
@Service
public class ClassificationService {

    private static final Logger log = LoggerFactory.getLogger(ClassificationService.class);

    // ---- Static constants ported verbatim from the Python "pattern" / split_terms_ref / ex_list ----

    private static final String SPLIT_PATTERN_STR =
            "(F/|W/|\\bW / |F / |\\bFOR\\b|\\bWITH\\b|\\bAT\\b|\\bINCLUDE\\b|\\bINCLUDING\\b|\\bAND\\b|" +
            "\\bCONSISTS\\b|\\bINCLUDES\\b|\\bWITHOUT\\b|\\bW/O\\b|\\bWITH OUT\\b|\\bUSE IN\\b)";
    private static final Pattern SPLIT_PATTERN = Pattern.compile(SPLIT_PATTERN_STR);

    private static final String[] SPLIT_TERMS_REF = {
            "BRAN", "BRAND NAME", "BRND", "DESIGNATION", "DOC", "DRAWING", "DRG", "DW", "DWG", "DWI",
            "EQUIP/TAG MODEL", "EQUIP/TAG SERIAL", "ITEM NO", "ITEM S/N", "MAKE", "MANUF", "MANUFACTURER",
            "MANUFACTURER/SUPPL NAME", "MFR", "MM:", "MNFR", "MOD", "MODEL", "MODEL/MACHINE NO", "OEM",
            "OEMPART NO", "ORDER NO", "P/ N", "P[.]N", "P[.]NO", "P[.]NUMBER", "PART LIST", "PART NO",
            "PART NUMBER", "PN", "POS", "POSITION NO", "POSITIONNO", "PT[.]NO", "REF", "REFERENCE", "S/N",
            "SERIAL", "SERIALNO", "SUPPLIER NAME", "SUPPLIER P / N", "SUPPLIER P/N", "SUPPLIER PART NO",
            "TAG/EQUIP NO", "TAG:"
    };

    private static final String[] EX_LIST_BASE = {
            "FOR", "F/", "W/", "W / ", "F / ", "WITH", "AT", "INCLUDE", "INCLUDING", "AND", "CONSISTS",
            "INCLUDES", "WITHOUT", "W/O", "WITH OUT", ","
    };

    private static final String EX_LIST_ALTERNATION;

    static {
        List<String> exList = new ArrayList<>();
        for (String s : EX_LIST_BASE) {
            exList.add(s);
        }
        for (String s : SPLIT_TERMS_REF) {
            exList.add(s);
        }
        EX_LIST_ALTERNATION = String.join("|", exList);
    }

    private static final Pattern EX_LIST_TRAILING_PATTERN = Pattern.compile("\\b(" + EX_LIST_ALTERNATION + ")\\b.*");

    // ---- Precompiled generic patterns (mirrors optimized_code.py's module-level _RE_* patterns) ----

    private static final Pattern RE_PAREN_CONTENT = Pattern.compile("\\([^()]*\\)");
    private static final Pattern RE_REMOVE_PARQUOTES = Pattern.compile("[()\"]");
    private static final Pattern RE_SPACES = Pattern.compile(" +");
    private static final Pattern RE_WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern RE_NON_ASCII = Pattern.compile("[^A-Z0-9\\s/,()'\".-]");
    private static final Pattern RE_DOT_FIX = Pattern.compile("([A-Z])\\.");
    private static final Pattern RE_MEASURE_UNITS = Pattern.compile("(\\d{1,4})(MM|CM|VDC|VAC|V|INCH|V AC|V DC|RPM|HZ)");
    private static final Pattern RE_INCH_IN = Pattern.compile("(\\d{1,4})\\s?IN\\b");
    private static final Pattern RE_INCH_QUOTE = Pattern.compile("(\\d{1,4})\\s?\"");
    private static final Pattern RE_O_RING = Pattern.compile("\"O\"\\s?RING");
    private static final Pattern RE_O_RINGSET = Pattern.compile("\"O\"\\s?RINGSET");
    private static final Pattern RE_Y_VARIANTS = Pattern.compile("\\b(WYE|'Y' TYPE|Y-TYPE|\" Y \"|\"Y\"|\" Y |Y)\\b");
    private static final Pattern RE_SPARE_PREFIX = Pattern.compile("\"{0,6}SPARE PARTS FOR:");
    private static final Pattern RE_PLANT_SPECIFIC = Pattern.compile(
            "PLANT SPECIFIC ITEMS-ESL,|PLANT SPECIFIC SPARES,|LOCO SPARES,|" +
            "MATERIAL DESCRIPTION:|THIS NOT FOR PURCHASING UNDER OBSERVATION");
    private static final Pattern RE_APPLICATION_ASSEMBLY = Pattern.compile("\\bAPPLICATION.*?ASSEMBLY\\b");
    private static final Pattern RE_NON_ALPHA = Pattern.compile("[^A-Za-z0-9\\s:'\\-/]");
    private static final Pattern RE_BOLT_NUT_WASHER = Pattern.compile("(NUT|WASHER)");
    private static final Pattern RE_HEX_VARIANTS = Pattern.compile("(HEX|HX|HEXAGON|HEXAGONAL)");

    private final ThirdPrefRepository thirdPrefRepository;
    private final SecondPrefRepository secondPrefRepository;
    private final FirstPrefRepository firstPrefRepository;
    private final LastPrefRepository lastPrefRepository;
    private final AlterWordRepository alterWordRepository;
    private final ExclusionRepository exclusionRepository;

    // In-memory caches, refreshed after data load (mirrors the Pandas DataFrames kept in memory)
    private volatile List<Object[]> classCheck = new ArrayList<>();   // Third_Pref  -> RULE3
    private volatile List<Object[]> objCheck = new ArrayList<>();     // class_check where TERM(Qualifier) notnull
    private volatile List<Object[]> check1 = new ArrayList<>();      // Second_Pref -> RULE2
    private volatile List<Object[]> r1Check = new ArrayList<>();      // First_Pref(NEW) -> RULE1
    private volatile List<Object[]> lstCheck = new ArrayList<>();      // Last_Pref -> LAST RULE
    private volatile List<AlterWord> alterWords = new ArrayList<>();
    private volatile List<Exclusion> exclusions = new ArrayList<>();

    public ClassificationService(ThirdPrefRepository thirdPrefRepository,
                                  SecondPrefRepository secondPrefRepository,
                                  FirstPrefRepository firstPrefRepository,
                                  LastPrefRepository lastPrefRepository,
                                  AlterWordRepository alterWordRepository,
                                  ExclusionRepository exclusionRepository) {
        this.thirdPrefRepository = thirdPrefRepository;
        this.secondPrefRepository = secondPrefRepository;
        this.firstPrefRepository = firstPrefRepository;
        this.lastPrefRepository = lastPrefRepository;
        this.alterWordRepository = alterWordRepository;
        this.exclusionRepository = exclusionRepository;
    }

    @PostConstruct
    public void loadCaches() {
        refreshCache();
    }

    /** Call after ExcelDataLoader (re)populates the DB to refresh the in-memory lookup lists. */
    public void refreshCache() {
        this.classCheck = thirdPrefRepository.findAllData();
        this.objCheck = thirdPrefRepository.findAllData();
        this.check1 = secondPrefRepository.findAllData();
        this.r1Check = firstPrefRepository.findAllData();
        this.lstCheck = lastPrefRepository.findAllData();
        this.alterWords = alterWordRepository.findAll();
        this.exclusions = exclusionRepository.findAll();
        log.info("Reference cache refreshed: Third_Pref={}, Second_Pref={}, First_Pref={}, Last_Pref={}, AlterWords={}, Exclusions={}",
                classCheck.size(), check1.size(), r1Check.size(), lstCheck.size(), alterWords.size(), exclusions.size());
    }

    // =========================================================================================
    // Small helpers replicating Python semantics (str.index() raising, word-boundary regex, etc.)
    // =========================================================================================

    private static Pattern wb(String term) {
        return Pattern.compile("\\b(" + term + ")\\b");
    }

    private static boolean search(String haystack, Pattern p) {
        return haystack != null && p.matcher(haystack).find();
    }

    private static String group(String haystack, Pattern p) {
        Matcher m = p.matcher(haystack);
        return m.find() ? m.group() : null;
    }

    /** Mirrors Python's str.index(x) which raises ValueError -> we throw so callers can try/catch. */
    private static int idx(String haystack, String needle) {
        if (haystack == null || needle == null) {
            throw new NoSuchElementException("null haystack/needle");
        }
        int i = haystack.indexOf(needle);
        if (i < 0) {
            throw new NoSuchElementException("'" + needle + "' not found");
        }
        return i;
    }

    // =========================================================================================
    // ALTER WORD REPLACEMENT  (Python: altr_word)
    // =========================================================================================

    public String altrWord(String desIn) {
        String des = desIn.toUpperCase();
        des = des.replace("\u00bf", "");
        des = des.replace("\u00d4", "O");
        des = des.replace("ASSEMBLY:", "ASSEMBLY");
        des = des.replace("SYSTEM:", "SYSTEM");
        des = RE_SPARE_PREFIX.matcher(des).replaceAll(" ");
        des = RE_PLANT_SPECIFIC.matcher(des).replaceAll("");
        des = des.replace(";", ",").replace(":", ",");
        des = des.replace("/", " / ");
        des = des.replace("'", " ' ");
        des = RE_NON_ASCII.matcher(des).replaceAll(" ");
        des = des.replace("\n", " ");
        des = RE_O_RING.matcher(des).replaceAll("ORING");
        des = RE_O_RINGSET.matcher(des).replaceAll("ORINGSET");
        des = RE_Y_VARIANTS.matcher(des).replaceAll("WYE ");
        des = des.replaceAll("\"[,]", "\" ,");
        des = RE_DOT_FIX.matcher(des).replaceAll("$1 ");
        des = RE_MEASURE_UNITS.matcher(des).replaceAll("$1 $2 ");

        Matcher inMatcher = RE_INCH_IN.matcher(des);
        if (inMatcher.find()) {
            String matched = inMatcher.group();
            String replaced = matched.replaceAll("(IN)\\b", " INCH");
            des = des.replaceFirst(Pattern.quote(matched), Matcher.quoteReplacement(replaced));
        }
        Matcher quoteMatcher = RE_INCH_QUOTE.matcher(des);
        if (quoteMatcher.find()) {
            String matched = quoteMatcher.group();
            String replaced = matched.replaceAll("\"", " INCH ");
            des = des.replaceFirst(Pattern.quote(matched), Matcher.quoteReplacement(replaced));
        }

        String firstToken = des.contains(",") ? des.substring(0, des.indexOf(',')) : des;
        if (firstToken.contains("BOLT")) {
            des = RE_WHITESPACE.matcher(des).replaceAll(" ");
            if (RE_BOLT_NUT_WASHER.matcher(des).find() && RE_HEX_VARIANTS.matcher(des).find()) {
                des = RE_HEX_VARIANTS.matcher(des).replaceAll(" ");
            }
        }
        if (des.contains("HOSE") && search(des, wb("AS"))) {
            des = wb("AS").matcher(des).replaceAll("ASSEMBLY");
        }
        des = des.replaceAll("\\bHX\\b", "HEXAGON");
        des = des.replaceAll("\\bPJ\\b", "PUP JOINT");
        des = des.replaceAll("\\b(GI|G\\.I|G\\.I\\.|GI\\.)\\b", "GALVANIZED IRON");
        des = des.replaceAll("\\bSS\\b", "STAINLESS STEEL");
        des = des.replaceAll("\\bCT\\b", "CONTACT");

        String x = RE_WHITESPACE.matcher(des).replaceAll(" ");

        for (AlterWord aw : alterWords) {
            String word = aw.getWord();
            String altr = aw.getAlterWord();
            if (altr == null || word == null || altr.length() <= 2) {
                continue;
            }
            if (x.contains(altr)) {
                // altr is a plain word/phrase (not a regex) -> match it literally at a word boundary
                Pattern p = Pattern.compile("\\b(" + Pattern.quote(altr) + ")(\\s|)\\b");
                x = p.matcher(x).replaceAll(Matcher.quoteReplacement(word + " "));

                Matcher appMatcher = wb("APPLICATION.*ASSEMBLY").matcher(x);
                if (appMatcher.find()) {
                    int split = x.indexOf("APPLICATION");
                    String before = x.substring(0, split);
                    String after = x.substring(split + "APPLICATION".length());
                    x = before + "APPLICATION" + after.replace("ASSEMBLY", "");
                }
            }
        }

        x = x.replace("-", " ");
        x = x.replaceAll(",+", ",");
        x = x.replaceAll(", +", ",");
        x = RE_WHITESPACE.matcher(x.trim()).replaceAll(" ");

        return x;
    }

    // =========================================================================================
    // WORDS EXEMPTION  (Python: exclusion_cndtn)
    // =========================================================================================

    public String exclusionCndtn(String des1In) {
        String des1 = des1In;
        des1 = RE_PAREN_CONTENT.matcher(des1).replaceAll(" ");
        des1 = RE_REMOVE_PARQUOTES.matcher(des1).replaceAll("");

        String des = des1.contains(",") ? des1.split(",", -1)[0] : des1;

        String desSplt = String.join("|", des.trim().isEmpty() ? new String[0] : des.trim().split("\\s+"));
        if (desSplt.isEmpty()) {
            return des1;
        }

        for (Exclusion ex : exclusions) {
            String obj = ex.getObject();
            String eTrm = ex.getExemptions();
            if (obj == null || eTrm == null) {
                continue;
            }
            if (!containsAny(obj, desSplt)) {
                continue;
            }
            if (!(search(des, wb(obj)) && search(des, wb(eTrm)))) {
                continue;
            }
            for (String trm : eTrm.split("\\|")) {
                try {
                    int f = des.indexOf(trm);
                    if (f == -1) {
                        continue;
                    }
                    Pattern trmPattern = wb(trm);
                    Matcher trmMatcher = trmPattern.matcher(des);
                    if (!trmMatcher.find()) {
                        continue;
                    }
                    String trmGroup = trmMatcher.group();
                    if (des.indexOf(trm, 1) != -1) {
                        if (idx(des, obj) <= idx(des, trmGroup)) {
                            des1 = removeAllMatches(des1, trmPattern);
                        }
                    } else {
                        if (idx(des, obj) <= idx(des, trmGroup)) {
                            des1 = removeAllMatches(des1, trmPattern);
                        }
                    }
                } catch (NoSuchElementException ignored) {
                    // Python: except: des1 = des1 (no-op)
                }
            }
        }
        return des1;
    }

    private static boolean containsAny(String regexAlternation, String candidateAlternation) {
        // Approximates the Pandas `.str.contains(des_splt, regex=True)` pre-filter: true if the
        // object's regex would plausibly match against at least one candidate token.
        try {
            return Pattern.compile(candidateAlternation).matcher(regexAlternation).find()
                    || Pattern.compile(regexAlternation).matcher(candidateAlternation.replace("|", " ")).find();
        } catch (Exception e) {
            return true; // fail open, exact word-boundary checks below still gate the match
        }
    }

    private static String removeAllMatches(String text, Pattern p) {
        Matcher m = p.matcher(text);
        StringBuilder found = new StringBuilder();
        Set<String> seen = new LinkedHashSet<>();
        while (m.find()) {
            seen.add(m.group());
        }
        if (seen.isEmpty()) {
            return text;
        }
        String alternation = String.join("|", seen);
        return Pattern.compile("\\b(" + alternation + ")\\b").matcher(text).replaceAll("");
    }

    // =========================================================================================
    // RULE1  (Python: rule1_cndtn, rule1)
    // =========================================================================================

    private static final class ClsMatch {
        final String cls;
        final String matched;

        ClsMatch(String cls, String matched) {
            this.cls = cls;
            this.matched = matched;
        }
    }

    private ClsMatch rule1Cndtn(String desIn) {
        String des = desIn;
        List<ClsMatch> matches = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Object[] fp : r1Check) {
            if (fp[0] == null) {
                continue;
            }

            String rule1 = (String) fp[1];
            String className = (String) fp[0];

            Matcher m = wb(rule1).matcher(des);
            if (m.find()) {
                String matchedText = m.group();
                String key = className + "||" + matchedText;

                if (seen.add(key)) {
                    matches.add(new ClsMatch(className, matchedText));
                }
            }
        }

        // Remove substrings: if matches[i].matched is a substring of matches[j].matched, drop i
        List<ClsMatch> filtered = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            boolean isSubstringOfAnother = false;
            for (int j = 0; j < matches.size(); j++) {
                if (i != j && matches.get(j).matched.contains(matches.get(i).matched)
                        && !matches.get(j).matched.equals(matches.get(i).matched)) {
                    isSubstringOfAnother = true;
                    break;
                }
            }
            if (!isSubstringOfAnother) {
                filtered.add(matches.get(i));
            }
        }

        if (filtered.isEmpty()) {
            return null;
        }

        ClsMatch best = null;
        int bestIndex = -1;
        for (ClsMatch cm : filtered) {
            int i = des.indexOf(cm.matched);
            if (i > bestIndex) {
                bestIndex = i;
                best = cm;
            }
        }
        return best;
    }

    private ClsMatch rule1(String desIn) {
        String des = desIn;
        des = RE_PAREN_CONTENT.matcher(des).replaceAll(" ");
        des = RE_REMOVE_PARQUOTES.matcher(des).replaceAll("");
        des = RE_SPACES.matcher(des).replaceAll(" ").trim();

        Matcher mat = SPLIT_PATTERN.matcher(des);
        if (mat.find() || des.contains(",")) {
            String x = des.replaceAll(SPLIT_PATTERN_STR + ".*", "");
            String first = x.contains(",") ? x.split(",", -1)[0] : x;
            return rule1Cndtn(first);
        }

        for (String term : SPLIT_TERMS_REF) {
            if (search(des, wb(term))) {
                String xSplt = des.replaceAll("\\b" + term + "\\b.*", "");
                return rule1Cndtn(xSplt);
            }
        }
        return rule1Cndtn(des);
    }


    private String objQualCndtn(String xIn) {
        String x = xIn;
        x = RE_REMOVE_PARQUOTES.matcher(x).replaceAll(" ");
        x = RE_SPACES.matcher(x).replaceAll(" ").trim();

        List<String> objct = new ArrayList<>();
        for (Object[] cc : classCheck) {
            String termObject = (String) cc[1];
            if (termObject == null) {
                continue;
            }
            Matcher m = wb(termObject).matcher(x);
            if (m.find()) {
                objct.add(termObject.contains("|") ? m.group() : termObject);
            }
        }
        List<String> objList = new ArrayList<>(new LinkedHashSet<>(objct));

        x = RE_NON_ALPHA.matcher(x).replaceAll(" ");
        x = RE_SPACES.matcher(x).replaceAll(" ").trim();

        if (objList.size() == 1) {
            return objList.get(0);
        } else if (objList.size() > 1) {
            List<String> obStr = new ArrayList<>();
            for (String s : objList) {
                obStr.add(s.replace("^", ""));
            }
            Set<String> minimal = new LinkedHashSet<>();
            for (String i : obStr) {
                boolean isSubsumed = false;
                for (String s : obStr) {
                    if (!i.equals(s) && s.contains(i) && (s.contains(" ") || s.contains("-"))) {
                        isSubsumed = true;
                        break;
                    }
                }
                if (!isSubsumed) {
                    minimal.add(i);
                }
            }
            if (!minimal.isEmpty()) {
                String best = null;
                int bestIdx = -1;
                for (String i : minimal) {
                    int last = x.lastIndexOf(i);
                    if (last > bestIdx) {
                        bestIdx = last;
                        best = i;
                    }
                }
                return best;
            }
        }
        return null;
    }

    private String objActual(String ob) {
        if (ob.contains("^")) {
            return ob.replace("^", "");
        }
        for (Object [] cc : classCheck) {
            if (cc[1] == null) {
                continue;
            }
            List<String> trm = java.util.Arrays.asList(((String) cc[1]).split("\\|"));
            if (trm.contains(ob) && ob.equals(cc[0])) {
                return (String) cc[1];
            }
        }
        return null;
    }

    private String objSynonym(String ob) {
        for (Object [] cc : classCheck) {
            String term = (String) cc[1];
            if (term == null) {
                continue;
            }
            List<String> trm = java.util.Arrays.asList(term.split("\\|"));
            if (term.contains("^")) {
                for (String syn : trm) {
                    if (syn.startsWith("^" + ob)) {
                        return term;
                    }
                }
            } else if (trm.contains(ob)) {
                return term;
            }
        }
        return null;
    }

    private String fnlObj(String ob1, String ob2) {
        if (ob1 == null && ob2 == null) {
            return null;
        }
        if (ob1 != null && ob1.equals(ob2)) {
            return ob1;
        }
        if (ob1 == null) {
            return ob2;
        }
        if (ob2 == null) {
            return ob1;
        }
        Matcher m = Pattern.compile(ob1).matcher(ob2);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    /** Returns {qualifier, qualifierMatchedString} or null (Python: obj_qualifier). */
    private String[] objQualifier(String x, String obj) {
        boolean spaceCondition = obj.contains(" ");
        List<String> qual1 = new ArrayList<>();
        List<String> qualStr = new ArrayList<>();

        String objClean = obj.contains("|") ? obj.substring(0, obj.indexOf('|')) : obj;

        for (Object [] cc : objCheck) {
            String termObject =(String)cc[2];
            String qual = (String) cc[3];
            String cls = (String) cc[1];
            if (termObject == null || qual == null || cls == null) {
                continue;
            }
            boolean matchesObj = search(termObject, wb(obj));
            boolean sameSpaceShape = spaceCondition == termObject.contains(" ");
            if (!(matchesObj && sameSpaceShape)) {
                continue;
            }
            Pattern p1 = wb(objClean + ", " + qual);
            Pattern p2 = wb(objClean + " " + qual);
            if (search(cls, p1) || search(cls, p2)) {
                Matcher qm = wb(qual).matcher(x);
                if (qm.find()) {
                    qual1.add(qual);
                    qualStr.add(qm.group());
                }
            }
        }

        List<String> dedupStr = new ArrayList<>(new LinkedHashSet<>(qualStr));
        if (dedupStr.isEmpty()) {
            return null;
        }

        List<Integer> indices = new ArrayList<>();
        for (String s : dedupStr) {
            indices.add(x.indexOf(s));
        }
        int minIdx = 0;
        for (int i = 1; i < indices.size(); i++) {
            if (indices.get(i) < indices.get(minIdx)) {
                minIdx = i;
            }
        }
        String chosenQual = qual1.get(qualStr.indexOf(dedupStr.get(minIdx)));
        String chosenStr = dedupStr.get(minIdx);

        if (chosenQual.contains("ASSEMBLY") && dedupStr.size() > 1) {
            List<String> remaining = new ArrayList<>(dedupStr);
            remaining.remove(minIdx);
            if (!remaining.isEmpty()) {
                int secondMinIdx = 0;
                int secondMinVal = x.indexOf(remaining.get(0));
                for (int i = 1; i < remaining.size(); i++) {
                    int v = x.indexOf(remaining.get(i));
                    if (v < secondMinVal) {
                        secondMinVal = v;
                        secondMinIdx = i;
                    }
                }
                String secondStr = remaining.get(secondMinIdx);
                String secondQual = qual1.get(qualStr.indexOf(secondStr));
                return new String[]{secondQual, secondStr};
            }
        }
        return new String[]{chosenQual, chosenStr};
    }

    private String fnlCls(String ob, String[] qu1) {
        for (Object[] cc : classCheck) {
            String cls = (String) cc[1];
            String termObject = (String) cc[2];
            if (cls == null || termObject == null || !termObject.contains(ob)) {
                continue;
            }
            if (qu1 != null && qu1.length > 0) {
                String qu = qu1[0];
                if (cls.contains(",")) {
                    String obPart = cls.replaceAll("(,).*", "");
                    String quPart = cls.replaceAll(".*(,)(\\s)", "");
                    if (search(obPart, wb(ob)) && search(qu, wb(quPart))) {
                        Matcher obMatch = Pattern.compile(ob).matcher(obPart);
                        Matcher quMatch = wb(quPart).matcher(qu);
                        if (obMatch.find() && quMatch.find()) {
                            String rebuilt = obMatch.group() + ", " + quMatch.group();
                            if (rebuilt.equals(cls)) {
                                return cls;
                            } else {
                                return ob.contains("|") ? ob.substring(0, ob.indexOf('|')) : ob;
                            }
                        }
                    }
                } else {
                    String obPart = cls.replaceAll("(\\s).*", "");
                    String quPart = cls.replaceAll(".*(\\s)", "");
                    if (search(obPart, wb(ob)) && search(qu, wb(quPart))) {
                        Matcher obMatch = Pattern.compile(ob).matcher(obPart);
                        Matcher quMatch = wb(quPart).matcher(qu);
                        if (obMatch.find() && quMatch.find()) {
                            String rebuilt = obMatch.group() + " " + quMatch.group();
                            if (rebuilt.equals(cls)) {
                                return cls;
                            } else {
                                return ob.contains("|") ? ob.substring(0, ob.indexOf('|')) : ob;
                            }
                        }
                    }
                }
            } else {
                if (ob.equals(cls)) {
                    return ob;
                } else if (ob.contains("|")) {
                    String obNoPipe = ob.substring(0, ob.indexOf('|'));
                    if (ob.equals(termObject) && obNoPipe.equals(cls)) {
                        return obNoPipe;
                    }
                }
            }
        }
        return null;
    }

    /** Returns {class, object, qualifier} or null (Python: find_obj). */
    private String[] findObj(List<String> splt, String des) {
        for (String raw : splt) {
            String i = exclusionCndtn(raw);
            String obj = objQualCndtn(i);

            String ob;
            if (obj != null) {
                String ob1 = objSynonym(obj);
                String ob2 = objActual(obj);
                ob = fnlObj(ob1, ob2);
            } else {
                ob = null;
            }

            String cls;
            String qu;
            if (ob != null) {
                String[] q1 = objQualifier(des, ob);
                if (q1 != null) {
                    cls = fnlCls(ob, q1);
                    qu = q1[1];
                } else {
                    cls = fnlCls(ob, null);
                    qu = "";
                }
            } else {
                cls = null;
                qu = "";
            }

            if (cls != null && !cls.isEmpty()) {
                return new String[]{cls, ob, qu};
            }
        }
        return null;
    }

    /** Returns {class, object, qualifier} or null (Python: object). */
    private String[] objectFn(String exDesIn, String des, String r1, int num) {
        String exDes = exDesIn;
        exDes = RE_PAREN_CONTENT.matcher(exDes).replaceAll(" ");
        exDes = RE_SPACES.matcher(exDes).replaceAll(" ");
        if (r1 != null && !r1.isEmpty()) {
            exDes = wb(r1).matcher(exDes).replaceAll("");
        }

        Matcher mat = SPLIT_PATTERN.matcher(exDes);
        if (mat.find()) {
            String match = mat.group();
            String x = exDes.replaceAll("\\b" + Pattern.quote(match) + "\\b.*", "");
            if (x.contains(",")) {
                String[] parts = x.split(",", -1);
                List<String> splt = num == 0
                        ? java.util.Collections.singletonList(parts[0])
                        : java.util.Arrays.asList(parts).subList(1, parts.length);
                return findObj(splt, des);
            } else {
                return findObj(java.util.Collections.singletonList(x), des);
            }
        } else {
            if (exDes.contains(",")) {
                String[] parts = exDes.split(",", -1);
                List<String> splt = num == 0
                        ? java.util.Collections.singletonList(parts[0])
                        : java.util.Arrays.asList(parts).subList(1, parts.length);
                return findObj(splt, des);
            } else {
                return findObj(java.util.Collections.singletonList(exDes), des);
            }
        }
    }

    // =========================================================================================
    // RULE2  (Python: frst_cls, frst_chk)
    // =========================================================================================

    /** Returns {class, matchedObjectString, matchedQualifier} or null (Python: frst_cls). */
    private String[] frstCls(String des, String des1, String cls, String stDes) {
        try {
            List<String> st = Arrays.asList(stDes.split("#", -1));
            if (st.isEmpty()) {
                return null;
            }

            // Remove excluded trailing words
            String stripped = EX_LIST_TRAILING_PATTERN.matcher(des1).replaceAll("");

            // First part must match
            Matcher firstMatcher = wb(st.get(0)).matcher(stripped);
            if (!firstMatcher.find()) {
                return null;
            }

            String stString = firstMatcher.group();

            List<String> stQual = new ArrayList<>();

            // Check remaining parts
            for (int i = 1; i < st.size(); i++) {
                Matcher matcher = wb(st.get(i)).matcher(des);

                if (!matcher.find()) {
                    return null;
                }

                stQual.add(matcher.group());
            }

            // If there are no qualifiers, return first match only
            if (stQual.isEmpty()) {
                return new String[]{cls, stString, ""};
            }

            // Find qualifier appearing earliest in description
            String stQua = stQual.get(0);
            int minPos = des.indexOf(stQua);

            for (int i = 1; i < stQual.size(); i++) {
                int pos = des.indexOf(stQual.get(i));
                if (pos >= 0 && pos < minPos) {
                    minPos = pos;
                    stQua = stQual.get(i);
                }
            }

            return new String[]{cls, stString, stQua};

        } catch (Exception e) {
            return null;
        }
    }

    private String[] frstChk(String des, String des1In) {
        for (Object[] sp : check1) {
            String cls = (String) sp[1];
            String chkDes = (String) sp[2];
            if (cls == null || chkDes == null) {
                continue;
            }
            if (frstCls(des, des1In, cls, chkDes) == null) {
                continue;
            }
            String des1 = des1In;
            des1 = RE_PAREN_CONTENT.matcher(des1).replaceAll(" ");
            des1 = RE_REMOVE_PARQUOTES.matcher(des1).replaceAll("");
            des1 = RE_SPACES.matcher(des1).replaceAll(" ");

            Matcher mat = SPLIT_PATTERN.matcher(des1);
            if (mat.find() || des1.contains(",")) {
                String x = des1.replaceAll(SPLIT_PATTERN_STR + ".*", "");
                String first = x.contains(",") ? x.split(",", -1)[0] : x;
                return frstCls(des, first, cls, chkDes);
            }
            for (String term : SPLIT_TERMS_REF) {
                if (search(des1, wb(term))) {
                    String xSplt = des1.replaceAll("\\b" + term + "\\b.*", "");
                    return frstCls(des, xSplt, cls, chkDes);
                }
            }
            return frstCls(des, des1, cls, chkDes);
        }
        return null;
    }

    // =========================================================================================
    // LAST RULE  (Python: last_cls)
    // =========================================================================================

    private String lastCls(String desIn) {
        String des = desIn;
        des = RE_PAREN_CONTENT.matcher(des).replaceAll(" ");
        des = RE_SPACES.matcher(des).replaceAll(" ");

        for (Object [] lp : lstCheck) {
            String lstCls = (String) lp[1];
            String lstObj = (String) lp[2];
            if (lstCls == null || lstObj == null) {
                continue;
            }
            Matcher mat = SPLIT_PATTERN.matcher(des);
            if (mat.find()) {
                String b = des.replaceAll(SPLIT_PATTERN_STR + ".*", "");
                b = b.contains(",") ? b.split(",", -1)[0] : b;
                if (search(b, wb(lstObj))) {
                    return lstCls;
                }
            } else {
                des = des.contains(",") ? des.split(",", -1)[0] : des;
                if (search(des, wb(lstObj))) {
                    return lstCls;
                }
            }
        }
        return null;
    }

    // =========================================================================================
    // final_object (kept for API parity with the Python module; not on the main decision path)
    // =========================================================================================

    public String finalObject(String des, String ob1, String ob2) {
        if (ob2 != null && !ob2.isEmpty()) {
            try {
                Matcher m = Pattern.compile(ob1).matcher(des);
                if (!m.find()) {
                    return ob2;
                }
                if (idx(des, m.group()) < idx(des, ob2)) {
                    return ob2;
                }
                return ob1;
            } catch (Exception e) {
                return ob2;
            }
        }
        return ob1;
    }

    // =========================================================================================
    // MAIN ENTRY POINT  (Python: total_class_procedure)
    // =========================================================================================

    public String totalClassProcedure(String d1) {
        String x1 = altrWord(d1 == null ? "" : d1);
        String x = exclusionCndtn(x1);
        String x2 = RE_PAREN_CONTENT.matcher(x).replaceAll(" ");
        x2 = RE_SPACES.matcher(x2).replaceAll(" ");

        ClsMatch r1 = rule1(x1);
        log.debug("normalized='{}' exclusion='{}'", x1, x);

        if (r1 != null) {
            String r1Cls = r1.cls;
            String r1Str = r1.matched;

            String[] scndCls = frstChk(x1, x);
            String[] obj = objectFn(x, x1, r1Str, 0);

            if (scndCls != null && obj == null) {
                String sCls = scndCls[0];
                String sStr = scndCls[1];
                String sQu = scndCls[2];
                try {
                    if (search(r1Str, wb(sStr))) {
                        if (search(r1Str, wb(sQu))) {
                            return r1Cls;
                        }
                        return idx(x2, r1Str) > idx(x1, sQu) ? sCls : r1Cls;
                    }
                    return idx(x2, r1Str) > idx(x1, sStr) ? r1Cls : sCls;
                } catch (NoSuchElementException e) {
                    return sCls;
                }

            } else if (scndCls == null && obj != null) {
                String cls = obj[0];
                String qual = obj[2];
                String objctMatch;
                try {
                    objctMatch = group(x, wb(obj[1]));
                    if (objctMatch == null) {
                        throw new NoSuchElementException();
                    }
                } catch (Exception e) {
                    return r1Cls;
                }
                try {
                    if (search(r1Str, wb(objctMatch))) {
                        if (search(r1Str, wb(qual))) {
                            return r1Cls;
                        }
                        try {
                            return idx(x1, r1Str) > idx(x1, qual) ? cls : r1Cls;
                        } catch (NoSuchElementException e) {
                            return r1Cls;
                        }
                    }
                    try {
                        if (idx(x2, r1Str) > idx(x2, objctMatch)) {
                            return r1Cls;
                        }
                        return cls;
                    } catch (NoSuchElementException e) {
                        return cls;
                    }
                } catch (Exception e) {
                    return cls;
                }

            } else if (scndCls != null && obj != null) {
                try {
                    String sCls = scndCls[0];
                    String sStr = scndCls[1];
                    String sQu = scndCls[2];
                    String cls = obj[0];
                    String qual = obj[2];
                    String objctMatch = group(x, wb(obj[1]));
                    if (objctMatch == null) {
                        return r1Cls;
                    }

                    if (objctMatch.equals(sStr)) {
                        try {
                            if (idx(x1, sQu) > idx(x1, qual) && qual != null && !qual.isEmpty()) {
                                return cls;
                            }
                            return sCls;
                        } catch (NoSuchElementException e) {
                            return sCls;
                        }
                    } else if (search(sStr, Pattern.compile(Pattern.quote(objctMatch)))) {
                        return cls.matches(".*[,\\s].*") ? cls : sCls;
                    } else if ((qual == null || qual.isEmpty()) && sQu != null && !sQu.isEmpty()) {
                        return sCls;
                    } else if (qual != null && !qual.isEmpty() && (sQu == null || sQu.isEmpty())) {
                        return cls;
                    } else if (idx(x1, objctMatch) > idx(x1, sStr)) {
                        return cls;
                    } else {
                        return r1Cls;
                    }
                } catch (Exception e) {
                    return r1Cls;
                }

            } else {
                return r1Cls;
            }

        } else {
            String[] scndCls = frstChk(x1, x);
            String[] obj = objectFn(x, x1, "", 0);

            if (scndCls != null && obj != null) {
                String sCls = scndCls[0];
                String sStr = scndCls[1];
                String sQu = scndCls[2];
                String cls = obj[0];
                String qual = obj[2];
                String objctMatch = group(x, wb(obj[1]));
                if (objctMatch == null) {
                    return scndCls[0];
                }

                try {
                    if (objctMatch.equals(sStr)) {
                        if (idx(x1, sQu) > idx(x1, qual) && qual != null && !qual.isEmpty()) {
                            return cls;
                        }
                        return sCls;
                    } else if (search(sStr, Pattern.compile(Pattern.quote(objctMatch)))) {
                        return cls.matches(".*[,\\s].*") ? cls : sCls;
                    } else if (search(objctMatch, Pattern.compile(Pattern.quote(sStr))) && !cls.matches(".*[,\\s].*")) {
                        return sCls;
                    } else if (search(objctMatch, Pattern.compile(Pattern.quote(sStr))) && cls.matches(".*[,\\s].*")) {
                        return cls;
                    } else if ((qual == null || qual.isEmpty()) && sQu != null && !sQu.isEmpty()) {
                        return sCls;
                    } else if (qual != null && !qual.isEmpty() && (sQu == null || sQu.isEmpty())) {
                        return sCls;
                    } else if (idx(x1, objctMatch) > idx(x1, sStr)) {
                        return cls;
                    } else if (idx(x1, objctMatch) < idx(x1, sStr)) {
                        return sCls;
                    }
                } catch (NoSuchElementException e) {
                    return sCls;
                }
                return fallback(x, x1);

            } else if (scndCls != null) {
                return scndCls[0];
            } else if (obj != null) {
                return obj[0];
            } else {
                return fallback(x, x1);
            }
        }
    }

    private String fallback(String x, String x1) {
        String[] altObj = objectFn(x, x1, "", 1);
        if (altObj != null) {
            return altObj[0];
        }
        return lastCls(x1);
    }

    /** Exposed for parity with complete_class.py's singularize() helper. */
    public String singularize(String word) {
        return TextInflectionUtil.singularize(word);
    }
}
