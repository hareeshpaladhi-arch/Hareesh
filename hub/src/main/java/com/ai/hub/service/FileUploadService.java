package com.ai.hub.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {

    /**
     * Accepts .xlsx, .xls, .csv, .tsv
     * Returns the uploaded file path.
     */
    public String prepareFileForBatch(MultipartFile file) throws IOException {

        String originalName = file.getOriginalFilename();

        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : "unknown";

        Path rawTemp = Files.createTempFile("batch_raw_", "." + ext);

        file.transferTo(rawTemp.toFile());

        switch (ext) {

        case "xlsx":
        case "xls":

            // Return Excel file directly
            return rawTemp.toAbsolutePath().toString();

        case "csv":
        case "tsv":
        case "tmp":

            Path csvTemp = Files.createTempFile("batch_csv_", ".csv");

            stripBomAndCopy(rawTemp, csvTemp);

            Files.deleteIfExists(rawTemp);

            return csvTemp.toAbsolutePath().toString();

        default:
            throw new IllegalArgumentException(
                    "Unsupported file type: " + ext
                            + ". Upload .xlsx, .xls, .csv or .tsv");
        }
    }

    private void stripBomAndCopy(Path source, Path destination)
            throws IOException {

        try (BOMInputStream bis = new BOMInputStream(
                new BufferedInputStream(Files.newInputStream(source)));
             BufferedWriter writer = Files.newBufferedWriter(destination)) {

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(bis));

            String line;

            while ((line = reader.readLine()) != null) {

                writer.write(line);
                writer.newLine();
            }
        }
    }
}