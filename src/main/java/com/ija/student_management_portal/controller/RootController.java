package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.entity.StudentProfilePicture;
import com.ija.student_management_portal.service.ProfilePictureService;
import com.ija.student_management_portal.dto.FeePayment;
import com.ija.student_management_portal.dto.PaginatedStudentResponse;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.dto.TransactionDTO;
import com.ija.student_management_portal.service.StudentService;
import com.ija.student_management_portal.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@Slf4j
public class RootController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ProfilePictureService profilePictureService;

    @GetMapping("/home")
    public String homePage(Model model) {
        return "forward:/index.html";
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
        return "forward:/index.html";
    }

    @PostMapping("/students/{id}/update")
    public String updateStudentDetails(@PathVariable String id,
                                       @ModelAttribute StudentDTO studentDTO,
                                       RedirectAttributes redirectAttributes) {
        String redirectUrl = UriComponentsBuilder.fromPath("/students/{id}")
                .buildAndExpand(id)
                .encode()
                .toUriString();
        try {
            studentService.updateStudent(id, studentDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Student details updated successfully.");
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update student {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating student {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
        }
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/accept/{studentId}")
    public String showAcceptFeeForm(@PathVariable String studentId, Model model) {
        return "forward:/index.html";
    }

    @PostMapping("/fees/accept")
    public String acceptFee(@ModelAttribute FeePayment feePayment) {

        TransactionDTO transactionDTO = TransactionDTO.builder()
                        .amount(feePayment.getAmount())
                        .transactionDate(LocalDateTime.now())
                        .paymentReceivedDate(feePayment.getPaymentReceivedDate())
                        .studentId(feePayment.getStudentId())
                        .paymentForMonth(feePayment.getPaymentForMonth())
                        .billNumber(feePayment.getBillNumber())
                        .build();

        transactionService.saveTransactionDetails(transactionDTO);
        return "redirect:/students/" + feePayment.getStudentId();
    }

    @GetMapping("/add")
    public String showAddStudentForm(Model model) {
        return "forward:/index.html";
    }

    @PostMapping("/students/add")
    public String registerStudent(@ModelAttribute StudentDTO studentDTO, RedirectAttributes redirectAttributes) {
        try {
            studentService.saveStudent(studentDTO);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to register student: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/add";
        }
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
        }
    }

    /**
     * Serve the compressed profile picture stored in the database.
     *
     * @param studentId the student whose picture is requested
     * @return the JPEG image bytes, or 404 if no picture is stored
     */
    @GetMapping("/students/{studentId}/profile-picture")
    @ResponseBody
    public ResponseEntity<byte[]> getStudentProfilePicture(@PathVariable String studentId) {
        return profilePictureService.getProfilePicture(studentId)
                .map(pic -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(pic.getContentType()))
                        .body(pic.getImageData()))
                .orElse(ResponseEntity.notFound().build());
    }
}
