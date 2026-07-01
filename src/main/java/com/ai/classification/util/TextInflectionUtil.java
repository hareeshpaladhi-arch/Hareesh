package com.ai.classification.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Lightweight replacement for Python's `inflect.engine().singular_noun()`,
 * which complete_class.py wrapped as:
 *
 *   def singularize(word):
 *       singular = _inflect.singular_noun(word)
 *       return word if singular is False else singular.upper()
 *
 * Java has no equivalent standard library, so this applies the common
 * English pluralization rules (irregulars, -ies, -ves, -xes/-ses/-shes/-ches,
 * bare -s) and otherwise returns the word unchanged, mirroring the
 * "already singular -> return as-is" behaviour of inflect.
 */
public final class TextInflectionUtil {

    private TextInflectionUtil() {
    }

    // Common irregular plural -> singular pairs seen in engineering/material text
    private static final java.util.Map<String, String> IRREGULARS = new java.util.HashMap<>();
    static {
        IRREGULARS.put("TEETH", "TOOTH");
        IRREGULARS.put("FEET", "FOOT");
        IRREGULARS.put("MEN", "MAN");
        IRREGULARS.put("WOMEN", "WOMAN");
        IRREGULARS.put("CHILDREN", "CHILD");
        IRREGULARS.put("KNIVES", "KNIFE");
        IRREGULARS.put("LEAVES", "LEAF");
        IRREGULARS.put("LIVES", "LIFE");
        IRREGULARS.put("SHELVES", "SHELF");
    }

    // Words that look plural but should NOT be singularized (uninflected / commonly false-positive)
    private static final Set<String> UNINFLECTED = new HashSet<>(Arrays.asList(
            "GAS", "BUS", "GLASS", "BRASS", "CROSS", "PRESS", "CHASSIS", "SERIES",
            "ANALYSIS", "BASIS", "AXIS", "STATUS", "APPARATUS", "ASSESS"
    ));

    /**
     * Returns the singular form of the word if it is recognisably plural,
     * otherwise returns the word unchanged (matching the Python fallback
     * where inflect.singular_noun() returns False for non-plural input).
     */
    public static String singularize(String word) {
        if (word == null) {
            return null;
        }
        String w = word.trim();
        if (w.isEmpty()) {
            return w;
        }

        String upper = w.toUpperCase();

        if (UNINFLECTED.contains(upper)) {
            return upper;
        }

        if (IRREGULARS.containsKey(upper)) {
            return IRREGULARS.get(upper);
        }

        if (upper.endsWith("IES") && upper.length() > 3) {
            return upper.substring(0, upper.length() - 3) + "Y";
        }
        if (upper.endsWith("VES") && upper.length() > 3) {
            return upper.substring(0, upper.length() - 3) + "FE";
        }
        if (upper.endsWith("XES") || upper.endsWith("SES") || upper.endsWith("SHES") || upper.endsWith("CHES")) {
            return upper.substring(0, upper.length() - 2);
        }
        if (upper.endsWith("S") && !upper.endsWith("SS") && upper.length() > 1) {
            return upper.substring(0, upper.length() - 1);
        }

        // Not recognisably plural -> return as-is (mirrors inflect's False -> original word)
        return upper;
    }
}
