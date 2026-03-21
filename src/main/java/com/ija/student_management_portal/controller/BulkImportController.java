package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.BulkImportResult;
import com.ija.student_management_portal.service.BulkImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
@RequestMapping("/bulk-import")
public class BulkImportController {

    @Autowired
    private BulkImportService bulkImportService;

    /**
     * Show bulk import page
     */
    @GetMapping
    public String showBulkImportPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("templateInfo", bulkImportService.getSampleExcelTemplate());

        return "bulk-import";
    }

    /**
     * Upload and import Excel file
     */
    @PostMapping("/upload")
    public String uploadAndImportStudents(@RequestParam("file") MultipartFile file,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
        try {
            log.info("Received file upload request: {}", file.getOriginalFilename());

            // Validate file
            if (file == null || file.isEmpty()) {
                redirectAttributes.addFlashAttribute("importResult",
                    BulkImportResult.builder()
                        .success(false)
                        .message("No file selected")
                        .totalRecords(0)
                        .successfulImports(0)
                        .failedImports(0)
                        .build());
                return "redirect:/bulk-import";
            }

            // Validate file size (10MB max)
            if (file.getSize() > 10 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("importResult",
                    BulkImportResult.builder()
                        .success(false)
                        .message("File size exceeds 10MB limit")
                        .totalRecords(0)
                        .successfulImports(0)
                        .failedImports(0)
                        .build());
                return "redirect:/bulk-import";
            }

            // Process import
            BulkImportResult result = bulkImportService.importStudentsFromExcel(file);

            // Store result in flash attributes
            redirectAttributes.addFlashAttribute("importResult", result);

            log.info("Import completed: {}", result.getMessage());

            return "redirect:/bulk-import";

        } catch (Exception e) {
            log.error("Error during file upload and import", e);
            redirectAttributes.addFlashAttribute("importResult",
                BulkImportResult.builder()
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .totalRecords(0)
                    .successfulImports(0)
                    .failedImports(0)
                    .build());
            return "redirect:/bulk-import";
        }
    }

    /**
     * Get import template info (as API)
     */
    @GetMapping("/template-info")
    @ResponseBody
    public String getTemplateInfo() {
        return bulkImportService.getSampleExcelTemplate();
    }
}
