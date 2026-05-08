package com.ai.login.service;
import com.ai.login.DTO.BatchTemplate;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

@Service
public class FileUploadService {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    /**
     * Accepts .xlsx, .xls, or .csv/.tsv
     * Converts Excel to tab-separated CSV and saves to a clean temp file.
     * Returns the path to a safe, OpenCSV-readable file.
     */
    public String prepareFileForBatch(MultipartFile file) throws IOException {

        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : "unknown";

        // Save raw upload to a temp file first
        Path rawTemp = Files.createTempFile("batch_raw_", "." + ext);
        file.transferTo(rawTemp.toFile());

        Path csvTemp = Files.createTempFile("batch_csv_", ".csv");

        switch (ext) {
            case "xlsx":
                convertExcelToCsv(rawTemp.toFile(), csvTemp, false);
                break;
            case "xls":
                convertExcelToCsv(rawTemp.toFile(), csvTemp, true);
                break;
            case "csv":
            case "tsv":
            case "tmp":  // handle temp file uploads
                stripBomAndCopy(rawTemp, csvTemp);
                break;
            default:
                throw new IllegalArgumentException(
                    "Unsupported file type: " + ext + ". Upload .xlsx, .xls, or .csv");
        }

        Files.deleteIfExists(rawTemp); // cleanup raw temp
        return csvTemp.toAbsolutePath().toString();
    }

    // ── Excel → tab-separated CSV ─────────────────────────────────────────
    private void convertExcelToCsv(File excelFile, Path csvOutput, boolean isXls)
            throws IOException {

        try (InputStream is = new FileInputStream(excelFile);
             Workbook workbook = isXls ? new HSSFWorkbook(is) : new XSSFWorkbook(is);
             BufferedWriter writer = Files.newBufferedWriter(csvOutput)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                if (isRowEmpty(row)) continue;

                StringBuilder sb = new StringBuilder();
                int lastCell = row.getLastCellNum();

                for (int i = 0; i < lastCell; i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = formatter.formatCellValue(cell).trim();

                    // Remove stray brackets from headers like "UNSPC]"
                    value = value.replaceAll("[\\[\\]]", "");

                    if (i > 0) sb.append("\t");
                    sb.append(value);
                }

                writer.write(sb.toString());
                writer.newLine();
            }
        }
    }

    // ── Strip UTF-8 BOM from CSV/TSV files ───────────────────────────────
    private void stripBomAndCopy(Path source, Path destination) throws IOException {
        try (BOMInputStream bis = new BOMInputStream(
                    new BufferedInputStream(Files.newInputStream(source)));
             BufferedWriter writer = Files.newBufferedWriter(destination)) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(bis));
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    // ── Helper: skip entirely blank rows ─────────────────────────────────
    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK
                    && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
}