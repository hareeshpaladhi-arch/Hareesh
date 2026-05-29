package com.ai.login.util;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.login.DAO.UserFileImportRepository;
import com.ai.login.DTO.BatchTemplate;


import java.io.*;
import java.nio.file.*;
import java.util.List;

@Component
public class FileItemWriter implements ItemWriter<BatchTemplate> {

    @Autowired
    private UserFileImportRepository repo;

    private static final String ERROR_DIR = "uploads/errors/";

    @Override
    public void write(List<? extends BatchTemplate> items) throws Exception {

        // ✅ Save valid data
        if (items != null && !items.isEmpty()) {
            repo.saveAll(items);
        }
    }

    // 🔷 OPTIONAL: If you still want error logging, call this from SkipListener
    public void writeErrors(List<String> errorLines) throws Exception {

        // ✅ Ensure directory exists
        Path dirPath = Paths.get(ERROR_DIR);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // ✅ Unique file per batch run
        String fileName = "error_" + System.currentTimeMillis() + ".csv";
        Path filePath = dirPath.resolve(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            // header
            writer.write("CLASS,SHORT_DESC,LONG_DESC,MATERIAL_TYPE,UNSPC,ERROR");
            writer.newLine();

            for (String line : errorLines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}