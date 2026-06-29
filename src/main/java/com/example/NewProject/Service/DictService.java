package com.example.NewProject.Service;

import com.example.NewProject.Entity.DictItemResponse;
import com.example.NewProject.Entity.DictModel;
import com.example.NewProject.Entity.DictResponse;
import com.example.NewProject.Entity.DictResponseBatch;
import com.example.NewProject.Repository.DictRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DictService {

    private static final String EMPTY = "Empty";
    private static final String NOT_FOUND = "Not Found";
    private static final String NO_MODIFIER = "NO MODIFIER";

    private final DictRepo dictRepo;

    private Set<String> nounSet = new HashSet<>();
    private final Map<String, Set<String>> nounToModifiers = new HashMap<>();
    private final Map<String, String> nounModifierCache = new HashMap<>();

    private String buildKey(String noun, String modifier) {
        return noun + "::" + modifier;
    }

    public DictService(DictRepo dictRepo) {
        this.dictRepo = dictRepo;
    }

    @PostConstruct
    public void loadDictionary() {

        List<DictModel> list = dictRepo.findAll();

        nounSet = list.stream()
                .map(DictModel::getNoun)
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        nounToModifiers.clear();

        for (DictModel d : list) {

            if (d.getNoun() == null || d.getModifier() == null) {
                continue;
            }

            nounToModifiers
                    .computeIfAbsent(d.getNoun().trim().toUpperCase(), k -> new HashSet<>())
                    .add(d.getModifier().trim().toUpperCase());
        }
    }

    private List<String> tokenize(String description) {
        if (description == null || description.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(description
                        .replace(",", " ")
                        .trim()
                        .toUpperCase()
                        .split("\\s+"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private List<String> findNoun(List<String> tokens) {

        //for single word noun
//       Set<String> tokenSet = new HashSet<>(tokens);
//
//        return nounSet.stream()
//                .filter(tokenSet::contains)
//                .sorted(Comparator.comparingInt(String::length).reversed())
//                .collect(Collectors.toList());

        //for double word noun  (ex:  turbo charger)
        String text = String.join(" ", tokens).toLowerCase();

        return nounSet.stream()
                .filter(noun -> text.contains(noun.toLowerCase()))
                .sorted(Comparator.comparingInt(String::length).reversed())
                .collect(Collectors.toList());

    }

    private String findModifier(String noun, List<String> tokens) {

        Set<String> modifiers =
                nounToModifiers.getOrDefault(noun, Collections.emptySet());

        String text = String.join(" ", tokens).toUpperCase();

        return modifiers.stream()
                .filter(text::contains)
                .max(Comparator.comparingInt(String::length)) // Optional: longest match
                .orElse(NO_MODIFIER);
    }

    public DictResponse findAllFromDescription(String description) {

        if (description == null || description.isBlank()) {
            return new DictResponse(EMPTY, Collections.emptyList());
        }

        List<String> tokens = tokenize(description);

        List<String> nouns = findNoun(tokens);

        if (nouns == null) {
            return new DictResponse(description, Collections.emptyList());
        }

        List<DictItemResponse> result = new ArrayList<>();

        for (String noun : nouns) {

            String modifier = findModifier(noun, tokens);

            String key = buildKey(noun, modifier);

            String nounModifier = nounModifierCache.computeIfAbsent(key,
                    k -> dictRepo.findNounModifier(noun, modifier));

            if (nounModifier == null || nounModifier.isBlank()) {
                nounModifier = NOT_FOUND;
            }

            result.add(new DictItemResponse(
                    noun,
                    modifier,
                    nounModifier
            ));
        }
        return new DictResponse(description, result);
    }

    public DictResponse processDescription(String description) {

        if (description == null || description.isBlank()) {
            return new DictResponse(description, Collections.emptyList());
        }

        List<String> tokens = tokenize(description);

        List<String> nouns = findNoun(tokens);

        if (nouns.isEmpty()) {
            return new DictResponse(description, Collections.emptyList());
        }

        List<DictItemResponse> result = new ArrayList<>();

        for (String noun : nouns) {

            String modifier = findModifier(noun, tokens);

            String key = buildKey(noun, modifier);

            String nounModifier = nounModifierCache.computeIfAbsent(key,
                    k -> dictRepo.findNounModifier(noun, modifier));

            if (nounModifier == null || nounModifier.isBlank()) {
                nounModifier = NOT_FOUND;
            }

            result.add(new DictItemResponse(
                    noun,
                    modifier,
                    nounModifier
            ));
        }

        return new DictResponse(description, result);
    }

    public DictResponseBatch findAllFromDescriptionList(List<String> descriptions) {

        if (descriptions == null || descriptions.isEmpty()) {
            return new DictResponseBatch(Collections.emptyList());
        }

        List<DictResponse> results = descriptions
                .stream() // optional: faster for large files
                .map(this::processDescription)
                .collect(Collectors.toList());

        return new DictResponseBatch(results);
    }

}