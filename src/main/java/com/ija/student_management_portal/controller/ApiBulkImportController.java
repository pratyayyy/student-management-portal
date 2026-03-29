package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.BulkImportResult;
import com.ija.student_management_portal.service.BulkImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/bulk-import")
@Slf4j
public class ApiBulkImportController {

    @Autowired
    private BulkImportService bulkImportService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "File is empty"));
            }
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("message", "File size exceeds 10MB limit"));
            }
            BulkImportResult result = bulkImportService.importStudentsFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Bulk import error", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Import failed: " + e.getMessage()));
        }
    }

    @GetMapping("/template-info")
    public ResponseEntity<String> getTemplateInfo() {
        return ResponseEntity.ok(bulkImportService.getSampleExcelTemplate());
    }
}
