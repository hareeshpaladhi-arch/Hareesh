package com.ai.hub.batch.util;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import com.ai.hub.dto.FieldMapping;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DynamicHeaderMapper {

    public static String createMappedFile(
            String sourceFile,
            String mappedData) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        List<FieldMapping> mappings = mapper.readValue(
                mappedData,
                new TypeReference<List<FieldMapping>>() {});

        Map<String, String> headerMap = mappings.stream()
                .collect(Collectors.toMap(
                        FieldMapping::getSourceField,
                        FieldMapping::getTargetField));

        Path mappedFile =
                Files.createTempFile("mapped_batch_", ".tsv");

        try (
                BufferedReader br = Files.newBufferedReader(
                        Paths.get(sourceFile),
                        StandardCharsets.UTF_8);

                BufferedWriter bw = Files.newBufferedWriter(
                        mappedFile,
                        StandardCharsets.UTF_8)
        ) {

            String headerLine = br.readLine();

            if (headerLine == null) {
                throw new RuntimeException("Empty file");
            }

            String[] headers = headerLine.split("\t", -1);

            for (int i = 0; i < headers.length; i++) {

                String sourceHeader = headers[i].trim();

                headers[i] =
                        headerMap.getOrDefault(
                                sourceHeader,
                                sourceHeader);
            }

            bw.write(String.join("\t", headers));
            bw.newLine();

            String line;

            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }
        }

        return mappedFile.toString();
    }
}