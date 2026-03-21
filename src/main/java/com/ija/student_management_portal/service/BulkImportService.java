package com.ija.student_management_portal.service;

import com.ija.student_management_portal.dto.BulkImportResult;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.dto.StudentRowModel;
import com.ija.student_management_portal.entity.StudentRollCounter;
import com.ija.student_management_portal.repository.StudentRepository;
import com.ija.student_management_portal.repository.StudentRollCounterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BulkImportService {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRollCounterRepository studentRollCounterRepository;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * Import students from Excel file
     * Validates each student before importing
     * Returns detailed import report
     */
    @Transactional
    public BulkImportResult importStudentsFromExcel(MultipartFile file) {
        long startTime = System.currentTimeMillis();
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;

        try {
            log.info("Starting bulk import from file: {}", file.getOriginalFilename());

            // Validate file
            if (file == null || file.isEmpty()) {
                errors.add("File is empty or not selected");
                return buildFailureResult(0, 0, errors, "No file provided");
            }

            if (!isValidExcelFile(file.getOriginalFilename())) {
                errors.add("Invalid file format. Only .xlsx files are supported");
                return buildFailureResult(0, 0, errors, "Invalid file format");
            }

            // Parse Excel file
            List<StudentRowModel> students = ExcelParserService.parseExcelFile(file);

            if (students.isEmpty()) {
                errors.add("No valid student records found in the file");
                return buildFailureResult(0, 0, errors, "No records to import");
            }

            int totalRecords = students.size();
            log.info("Parsed {} student records from Excel", totalRecords);

            // Import each student
            for (int i = 0; i < students.size(); i++) {
                StudentRowModel student = students.get(i);
                int rowNumber = i + 2; // +2 because row 1 is header and row numbers are 1-based

                try {
                    // Validate student
                    List<String> validationErrors = ExcelParserService.validateStudent(student, rowNumber);

                    if (!validationErrors.isEmpty()) {
                        failedImports++;
                        errors.addAll(validationErrors);
                        log.warn("Validation failed for row {}: {} - Student data: name='{}', phone='{}', standard='{}', address='{}', guardian='{}'",
                            rowNumber, validationErrors, student.getName(), student.getPhoneNumber(),
                            student.getStandard(), student.getAddress(), student.getGuardiansName());
                        continue;
                    }

                    // Check for duplicates (by phone number)
                    if (studentRepository.findByPhoneNumber(student.getPhoneNumber()).isPresent()) {
                        failedImports++;
                        String errorMsg = "Row " + rowNumber + ": Student with phone number " +
                                        student.getPhoneNumber() + " already exists";
                        errors.add(errorMsg);
                        log.warn(errorMsg);
                        continue;
                    }

                    // Save student
                    studentService.saveStudent(convertRowObjToDbObj(student));
                    successfulImports++;
                    log.info("Successfully imported student: {} (Row {})", student.getName(), rowNumber);

                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Row " + rowNumber + ": " + e.getMessage();
                    errors.add(errorMsg);
                    log.error("Error importing student at row {}: {}", rowNumber, e.getMessage(), e);
                }
            }

            long importTimeMs = System.currentTimeMillis() - startTime;

            log.info("Bulk import completed. Total: {}, Success: {}, Failed: {}, Time: {}ms",
                    totalRecords, successfulImports, failedImports, importTimeMs);

            return BulkImportResult.builder()
                    .totalRecords(totalRecords)
                    .successfulImports(successfulImports)
                    .failedImports(failedImports)
                    .errors(errors)
                    .success(failedImports == 0)
                    .message(String.format("Imported %d out of %d students successfully",
                            successfulImports, totalRecords))
                    .importTimestampMillis(importTimeMs)
                    .build();

        } catch (IOException e) {
            String errorMsg = "Error reading Excel file: " + e.getMessage();
            errors.add(errorMsg);
            log.error(errorMsg, e);
            return buildFailureResult(0, 0, errors, "File reading error");

        } catch (Exception e) {
            String errorMsg = "Unexpected error during import: " + e.getMessage();
            errors.add(errorMsg);
            log.error(errorMsg, e);
            return buildFailureResult(0, 0, errors, "Import failed");
        }
    }

    private StudentDTO convertRowObjToDbObj(StudentRowModel student) {
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setName(student.getName());
        studentDTO.setPhoneNumber(student.getPhoneNumber());
        studentDTO.setAlternateNumber(student.getAlternateNumber());
        studentDTO.setStandard(student.getStandard());
        studentDTO.setAddress(student.getAddress());
        studentDTO.setGuardiansName(student.getGuardiansName());

        int admissionYear = LocalDateTime.now().getYear();
        StudentRollCounter counter = studentRollCounterRepository.findForUpdate(admissionYear)
                .orElseGet(() -> {
                    StudentRollCounter c = new StudentRollCounter();
                    c.setAdmissionYear(admissionYear);
                    c.setLastNumber(0);
                    return studentRollCounterRepository.save(c);
                });

        counter.setLastNumber(counter.getLastNumber() + 1);
        studentRollCounterRepository.saveAndFlush(counter);

        String studentId = admissionYear + "-" + String.format("%04d", counter.getLastNumber());

        studentDTO.setStudentId(studentId);

        return studentDTO;
    }

    /**
     * Validate file extension
     */
    private boolean isValidExcelFile(String filename) {
        return filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls"));
    }

    /**
     * Build failure result
     */
    private BulkImportResult buildFailureResult(int total, int successful, List<String> errors, String message) {
        return BulkImportResult.builder()
                .totalRecords(total)
                .successfulImports(successful)
                .failedImports(total - successful)
                .errors(errors)
                .success(false)
                .message(message)
                .importTimestampMillis(0)
                .build();
    }

    /**
     * Get sample Excel template
     * Returns instructions for Excel format
     */
    public String getSampleExcelTemplate() {
        return """
                SAMPLE EXCEL FORMAT:
                
                Column A: Student Name (Required)
                Column B: Phone Number (Required, 10 digits)
                Column C: Alternate Number (Optional)
                Column D: Standard/Class (Required)
                Column E: Address (Required)
                Column F: Guardian's Name (Required)
                
                Example:
                Row 1 (Header): Name | Phone Number | Alternate Number | Standard | Address | Guardians Name
                Row 2: John Doe | 9876543210 | 9876543211 | 10 | 123 Main St | Mr. Doe
                Row 3: Jane Smith | 8765432109 | 8765432108 | 9 | 456 Oak Ave | Mrs. Smith
                
                Notes:
                - Phone number must be 10 digits
                - All required fields must be filled
                - Duplicate phone numbers will be skipped
                - Maximum file size: 10MB
                """;
    }
}
