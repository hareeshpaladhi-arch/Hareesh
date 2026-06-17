package com.ai.hub.batch.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@StepScope
public class ExcelItemReader implements ItemReader<Map<String, String>> {

    private Iterator<Row> rowIterator;
    private List<String> headers;
    private Workbook workbook;
    private InputStream inputStream;

    private Map<String, String> columnMapping = new HashMap<>();

    public ExcelItemReader(
            @Value("#{jobParameters['filePath']}") String path,
            @Value("#{jobParameters['mappedData']}") String mappedData)
            throws Exception {

        System.out.println("Reading File : " + path);
        System.out.println("Mapped Data : " + mappedData);

        // Build mapping lookup
        if (mappedData != null && !mappedData.trim().isEmpty()) {

            ObjectMapper mapper = new ObjectMapper();

            List<Map<String, String>> mappings = mapper.readValue(
                    mappedData,
                    new TypeReference<List<Map<String, String>>>() {
                    });

            for (Map<String, String> mapping : mappings) {

                String sourceField = mapping.get("sourceField");
                String targetField = mapping.get("targetField");

                if (sourceField != null && targetField != null) {

                    columnMapping.put(
                            sourceField.trim(),
                            targetField.trim());
                }
            }
        }

        inputStream = new FileInputStream(path);

        if (path.toLowerCase().endsWith(".xlsx")) {

            workbook = new XSSFWorkbook(inputStream);

        } else if (path.toLowerCase().endsWith(".xls")) {

            workbook = new HSSFWorkbook(inputStream);

        } else {

            throw new RuntimeException(
                    "Unsupported Excel file format : " + path);
        }

        Sheet sheet = workbook.getSheetAt(0);

        rowIterator = sheet.iterator();

        if (!rowIterator.hasNext()) {
            throw new RuntimeException("Excel file is empty");
        }

        Row headerRow = rowIterator.next();

        headers = new ArrayList<>();

        for (Cell cell : headerRow) {

            String header = getCellValue(cell).trim();

            headers.add(header);
        }

        System.out.println("Excel Headers : " + headers);
        System.out.println("Column Mapping : " + columnMapping);
    }

    @Override
    public Map<String, String> read() {

        if (rowIterator == null || !rowIterator.hasNext()) {

            closeResources();

            return null;
        }

        Row row = rowIterator.next();

        Map<String, String> data = new HashMap<>();

        for (int i = 0; i < headers.size(); i++) {

            String sourceHeader = headers.get(i);

            String targetHeader =
                    columnMapping.getOrDefault(
                            sourceHeader,
                            sourceHeader);

            Cell cell = row.getCell(i);

            data.put(
                    targetHeader,
                    getCellValue(cell));
        }

        return data;
    }

    private String getCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {

        case STRING:
            return cell.getStringCellValue().trim();

        case NUMERIC:
            return String.valueOf(cell.getNumericCellValue());

        case BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());

        case FORMULA:
            return cell.getCellFormula();

        default:
            return "";
        }
    }

    private void closeResources() {

        try {

            if (workbook != null) {
                workbook.close();
            }

            if (inputStream != null) {
                inputStream.close();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}