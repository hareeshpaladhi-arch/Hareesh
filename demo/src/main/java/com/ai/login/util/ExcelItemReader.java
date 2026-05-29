package com.ai.login.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.monitorjbl.xlsx.StreamingReader;

@Component
@StepScope
public class ExcelItemReader implements ItemReader<Map<String, String>> {

    private Iterator<Row> rowIterator;
    private List<String> headers;
    private Workbook workbook; // ✅ keep reference to close later
    private InputStream inputStream;

    public ExcelItemReader(@Value("#{jobParameters['filePath']}") String path) throws Exception {

        inputStream = new FileInputStream(path);

        workbook = StreamingReader.builder()
                .rowCacheSize(100)     // memory control
                .bufferSize(4096)
                .open(inputStream);

        Sheet sheet = workbook.getSheetAt(0);
        rowIterator = sheet.iterator();

        // ✅ Safe header read
        if (!rowIterator.hasNext()) {
            throw new RuntimeException("Excel file is empty");
        }

        Row headerRow = rowIterator.next();
        headers = new ArrayList<>();

        for (Cell cell : headerRow) {
            headers.add(getCellValue(cell));
        }
    }

    @Override
    public Map<String, String> read() {

        if (rowIterator == null || !rowIterator.hasNext()) {
            closeResources(); // ✅ close when done
            return null;
        }

        Row row = rowIterator.next();
        Map<String, String> map = new HashMap<>();

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.getCell(i);
            map.put(headers.get(i), getCellValue(cell));
        }

        return map;
    }

    // ✅ Proper cell value handling
    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    // ✅ Close resources (VERY IMPORTANT)
    private void closeResources() {
        try {
            if (workbook != null) workbook.close();
            if (inputStream != null) inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}