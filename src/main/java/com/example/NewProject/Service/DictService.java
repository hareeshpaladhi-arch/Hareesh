package com.example.NewProject.Service;

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

        return Arrays.stream(description.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .toList();
    }

    private String findNoun(List<String> tokens) {

        return tokens.stream()
                .filter(nounSet::contains)
                .findFirst()
                .orElse(null);
    }

    private String findModifier(String noun, List<String> tokens) {

        Set<String> modifiers =
                nounToModifiers.getOrDefault(noun, Collections.emptySet());

        return tokens.stream()
                .filter(modifiers::contains)
                .findFirst()
                .orElse(NO_MODIFIER);
    }

    public DictResponse findAllFromDescription(String description) {

        if (description == null || description.isBlank()) {
            return new DictResponse(EMPTY, "", "", "");
        }

        List<String> tokens = tokenize(description);

        String noun = findNoun(tokens);

        if (noun == null) {
            return new DictResponse(description, NOT_FOUND, "", "");
        }

        String modifier = findModifier(noun, tokens);

        String nounModifier = dictRepo.findNounModifier(noun, modifier);

        if (nounModifier == null || nounModifier.isBlank()) {
            nounModifier = NOT_FOUND;
        }

        return new DictResponse(
                description,
                noun,
                modifier,
                nounModifier
        );
    }

    public DictResponse processDescription(String description) {

        if (description == null || description.isBlank()) {
            return new DictResponse(EMPTY, "", "", "");
        }

        List<String> tokens = tokenize(description);
        String noun = findNoun(tokens);

        if (noun == null) {
            return new DictResponse(description, NOT_FOUND, "", "");
        }

        String modifier = findModifier(noun, tokens);

        String key = buildKey(noun, modifier);

        String nounModifier = nounModifierCache.computeIfAbsent(key, k ->
                dictRepo.findNounModifier(noun, modifier)
        );

        if (nounModifier == null || nounModifier.isBlank()) {
            nounModifier = NOT_FOUND;
        }

        return new DictResponse(description, noun, modifier, nounModifier);
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