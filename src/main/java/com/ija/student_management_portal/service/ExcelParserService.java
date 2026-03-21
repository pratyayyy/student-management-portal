package com.ija.student_management_portal.service;

import com.ija.student_management_portal.dto.BulkImportResult;
import com.ija.student_management_portal.dto.StudentDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class ExcelParserService {

    /**
     * Parse Excel file and extract student data
     * Expected columns: Name | Phone Number | Alternate Number | Standard | Address | Guardians Name
     */
    public static List<StudentDTO> parseExcelFile(MultipartFile file) throws IOException {
        List<StudentDTO> students = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row and iterate through data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    StudentDTO student = extractStudentFromRow(row);
                    if (student != null) {
                        students.add(student);
                    }
                } catch (Exception e) {
                    log.warn("Error parsing row {}: {}", i + 1, e.getMessage());
                }
            }
        }

        return students;
    }

    /**
     * Extract student data from a single row
     * Expected order: Name(0) | Phone(1) | Alternate(2) | Standard(3) | Address(4) | Guardian(5)
     */
    private static StudentDTO extractStudentFromRow(Row row) {
        try {
            String name = getCellValueAsString(row.getCell(0));
            String phone = getCellValueAsString(row.getCell(1));
            String alternate = getCellValueAsString(row.getCell(2));
            String standard = getCellValueAsString(row.getCell(3));
            String address = getCellValueAsString(row.getCell(4));
            String guardian = getCellValueAsString(row.getCell(5));

            // Validate required fields
            if (name == null || name.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty()) {
                return null;
            }

            return StudentDTO.builder()
                    .name(name.trim())
                    .phoneNumber(phone.trim())
                    .alternateNumber(alternate != null ? alternate.trim() : "")
                    .standard(standard != null ? standard.trim() : "")
                    .address(address != null ? address.trim() : "")
                    .guardiansName(guardian != null ? guardian.trim() : "")
                    .build();
        } catch (Exception e) {
            log.warn("Error extracting student from row: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get cell value as string regardless of cell type
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return null;
        }
    }

    /**
     * Check if a row is completely empty
     */
    private static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !cell.toString().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validate student data
     */
    public static List<String> validateStudent(StudentDTO student, int rowNumber) {
        List<String> errors = new ArrayList<>();

        if (student.getName() == null || student.getName().trim().isEmpty()) {
            errors.add("Row " + rowNumber + ": Student name is required");
        }

        if (student.getPhoneNumber() == null || student.getPhoneNumber().trim().isEmpty()) {
            errors.add("Row " + rowNumber + ": Phone number is required");
        } else if (!isValidPhoneNumber(student.getPhoneNumber())) {
            errors.add("Row " + rowNumber + ": Invalid phone number format");
        }

        if (student.getStandard() == null || student.getStandard().trim().isEmpty()) {
            errors.add("Row " + rowNumber + ": Standard/Class is required");
        }

        if (student.getAddress() == null || student.getAddress().trim().isEmpty()) {
            errors.add("Row " + rowNumber + ": Address is required");
        }

        if (student.getGuardiansName() == null || student.getGuardiansName().trim().isEmpty()) {
            errors.add("Row " + rowNumber + ": Guardian's name is required");
        }

        return errors;
    }

    /**
     * Validate phone number format (basic validation)
     */
    private static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("\\d{10}");
    }
}
