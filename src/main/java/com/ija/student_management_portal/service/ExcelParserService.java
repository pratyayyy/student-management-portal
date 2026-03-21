package com.ija.student_management_portal.service;

import com.ija.student_management_portal.dto.BulkImportResult;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.dto.StudentRowModel;
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
    public static List<StudentRowModel> parseExcelFile(MultipartFile file) throws IOException {
        List<StudentRowModel> students = new ArrayList<>();

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
                    StudentRowModel student = extractStudentFromRow(row);
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
    private static StudentRowModel extractStudentFromRow(Row row) {
        try {
            String name = getCellValueAsString(row.getCell(0));
            String phone = getCellValueAsString(row.getCell(1));
            String alternate = getCellValueAsString(row.getCell(2));
            String standard = getCellValueAsString(row.getCell(3));
            String address = getCellValueAsString(row.getCell(4));
            String guardian = getCellValueAsString(row.getCell(5));

            // Debug logging to troubleshoot guardian name issue
            if (name != null && !name.isEmpty()) {
                log.debug("Extracted row - Name: '{}', Phone: '{}', Guardian: '{}'",
                    name, phone, guardian);
            }

            // Validate required fields
            if (name == null || name.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty()) {
                return null;
            }

            StudentRowModel studentRowObj = new StudentRowModel();
            studentRowObj.setName(name);
            studentRowObj.setPhoneNumber(phone);
            studentRowObj.setAlternateNumber(alternate);
            studentRowObj.setStandard(standard);
            studentRowObj.setAddress(address);
            studentRowObj.setGuardiansName(guardian);

            return studentRowObj;
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

        try {
            switch (cell.getCellType()) {
                case STRING:
                    String value = cell.getStringCellValue();
                    return (value != null) ? value.trim() : null;
                case NUMERIC:
                    // Check if it's a date
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString().trim();
                    }
                    // For numeric values, convert to string
                    double numValue = cell.getNumericCellValue();
                    // Check if it's an integer
                    if (numValue == Math.floor(numValue)) {
                        return String.valueOf((long) numValue).trim();
                    } else {
                        return String.valueOf(numValue).trim();
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue()).trim();
                case FORMULA:
                    // Try to get the cached value first
                    try {
                        return cell.getStringCellValue().trim();
                    } catch (Exception e) {
                        // If it's a numeric formula result
                        double formulaResult = cell.getNumericCellValue();
                        if (formulaResult == Math.floor(formulaResult)) {
                            return String.valueOf((long) formulaResult).trim();
                        }
                        return String.valueOf(formulaResult).trim();
                    }
                case BLANK:
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("Error converting cell value to string: {}", e.getMessage());
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
    public static List<String> validateStudent(StudentRowModel student, int rowNumber) {
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
