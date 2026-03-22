package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.FeePayment;
import com.ija.student_management_portal.dto.PaginatedStudentResponse;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.dto.TransactionDTO;
import com.ija.student_management_portal.entity.Student;
import com.ija.student_management_portal.service.StudentService;
import com.ija.student_management_portal.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@Slf4j
public class RootController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/home")
    public String homePage(Model model) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Check if user has ADMIN role
        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        log.info("Home page accessed by user: {} (Admin: {})", username, isAdmin);

        List<StudentDTO> students = studentService.getAllStudents();
        model.addAttribute("students", students);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("username", username);

        return "home";
    }

    /**
     * REST API endpoint for searching and paginating students
     * @param query search query (student name)
     * @param page page number (0-indexed)
     * @param pageSize number of records per page
     * @return paginated response with students
     */
    @GetMapping("/api/students/search")
    @ResponseBody
    public ResponseEntity<PaginatedStudentResponse> searchStudents(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {

        log.info("Search API called with query='{}', page={}, pageSize={}", query, page, pageSize);

        try {
            PaginatedStudentResponse response = studentService.searchStudents(query, page, pageSize);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in search API", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * REST API endpoint for getting all students with pagination
     * @param page page number (0-indexed)
     * @param pageSize number of records per page
     * @return paginated response with all students
     */
    @GetMapping("/api/students/paginated")
    @ResponseBody
    public ResponseEntity<PaginatedStudentResponse> getPaginatedStudents(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {

        log.info("Paginated students API called with page={}, pageSize={}", page, pageSize);

        try {
            PaginatedStudentResponse response = studentService.getAllStudentsWithPagination(page, pageSize);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in paginated students API", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/students/{id}")
    public String studentDetails(@PathVariable String id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<StudentDTO> student = studentService.getStudentById(id);
        List<TransactionDTO> transactions = transactionService.getTransactionById(id);

        model.addAttribute("student", student.get());
        model.addAttribute("transactions", transactions);
        model.addAttribute("username", username);

        return "student-details";
    }

    @GetMapping("/accept/{studentId}")
    public String showAcceptFeeForm(@PathVariable String studentId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        FeePayment feePayment = new FeePayment();
        feePayment.setStudentId(studentId);
        model.addAttribute("feePayment", feePayment);
        model.addAttribute("username", username);
        return "accept_fee";
    }

    @PostMapping("/fees/accept")
    public String acceptFee(@ModelAttribute FeePayment feePayment) {

        TransactionDTO transactionDTO = TransactionDTO.builder()
                        .amount(feePayment.getAmount())
                        .transactionDate(LocalDateTime.now())
                        .paymentReceivedDate(feePayment.getPaymentReceivedDate())
                        .studentId(feePayment.getStudentId())
                        .build();

        transactionService.saveTransactionDetails(transactionDTO);
        return "redirect:/students/" + feePayment.getStudentId();
    }

    @GetMapping("/add")
    public String showAddStudentForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("student", new StudentDTO());
        model.addAttribute("username", username);
        return "add_student";
    }

    @PostMapping("/students/add")
    public String registerStudent(@ModelAttribute StudentDTO studentDTO) {
        studentService.saveStudent(studentDTO);
        return "redirect:/home"; // Redirect to home page or student list
    }

    /**
     * Upload profile picture for a student
     * @param studentId the student ID
     * @param file the image file
     * @return JSON response with picture URL and status
     */
    @PostMapping("/students/{studentId}/upload-picture")
    @ResponseBody
    public ResponseEntity<?> uploadStudentPicture(@PathVariable String studentId,
                                                   @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            StudentDTO studentDTO = studentService.uploadStudentPicture(studentId, file);
            response.put("success", true);
            response.put("message", "Profile picture uploaded successfully");
            response.put("pictureUrl", studentDTO.getProfilePictureUrl());

            log.info("Profile picture uploaded successfully for student: {}", studentId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Validation error uploading picture for student: {}: {}", studentId, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IOException e) {
            log.error("Error uploading picture for student: {}", studentId, e);
            response.put("success", false);
            response.put("message", "Failed to upload profile picture");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete profile picture for a student
     * @param studentId the student ID
     * @return JSON response with status
     */
    @DeleteMapping("/students/{studentId}/delete-picture")
    @ResponseBody
    public ResponseEntity<?> deleteStudentPicture(@PathVariable String studentId) {
        Map<String, Object> response = new HashMap<>();

        try {
            studentService.deleteStudentPicture(studentId);
            response.put("success", true);
            response.put("message", "Profile picture deleted successfully");

            log.info("Profile picture deleted successfully for student: {}", studentId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Validation error deleting picture for student: {}: {}", studentId, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IOException e) {
            log.error("Error deleting picture for student: {}", studentId, e);
            response.put("success", false);
            response.put("message", "Failed to delete profile picture");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
