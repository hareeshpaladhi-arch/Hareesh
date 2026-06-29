package com.example.NewProject.Controller;


import com.example.NewProject.Entity.DictModel;
import com.example.NewProject.Entity.DictResponse;
import com.example.NewProject.Entity.DictResponseBatch;
import com.example.NewProject.Service.DictService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
public class DictController {

    @Autowired
    private DictService dictService;

    @GetMapping("/Noun-Modifier")
    public DictResponse getDictionary(@RequestParam String description) {
        return dictService.findAllFromDescription(description);
    }


    @PostMapping(value = "/Noun-Modifier-File", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DictResponseBatch getDictionaryFile(@RequestParam("file") MultipartFile file) {
        return findAllFromExcel(file);
    }

    public DictResponseBatch findAllFromExcel(MultipartFile file) {
        List<String> descriptions = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;

            for (Row row : sheet) {

                if (firstRow) {
                    firstRow = false;
                    continue;
                }

                Cell cell = row.getCell(0);

                if (cell != null) {
                    String value = formatter.formatCellValue(cell).trim();

                    if (!value.isEmpty()) {
                        descriptions.add(value);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }

        return dictService.findAllFromDescriptionList(descriptions);

    }

}

