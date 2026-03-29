package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.PaginatedStudentResponse;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.service.ProfilePictureService;
import com.ija.student_management_portal.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@Slf4j
public class ApiStudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private ProfilePictureService profilePictureService;

    // ── Search & Pagination ─────────────────────────────────────────

    @GetMapping("/search")
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

    @GetMapping("/paginated")
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

    // ── CRUD ────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudent(@PathVariable String id) {
        Optional<StudentDTO> student = studentService.getStudentById(id);
        return student.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody StudentDTO studentDTO) {
        try {
            studentService.saveStudent(studentDTO);
            return ResponseEntity.ok(Map.of("message", "Student added successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating student", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to add student"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable String id, @RequestBody StudentDTO studentDTO) {
        try {
            studentService.updateStudent(id, studentDTO);
            Optional<StudentDTO> updated = studentService.getStudentById(id);
            return updated.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating student {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to update student"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable String id) {
        try {
            studentService.deleteStudentById(id);
            return ResponseEntity.ok(Map.of("message", "Student deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting student {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to delete student"));
        }
    }

    // ── Profile Picture ─────────────────────────────────────────────

    @PostMapping("/{studentId}/upload-picture")
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
            response.put("pictureUrl", "/api/students/" + studentId + "/profile-picture");
            log.info("Profile picture uploaded successfully for student: {}", studentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error uploading picture for student {}: {}", studentId, e.getMessage());
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

    @DeleteMapping("/{studentId}/delete-picture")
    public ResponseEntity<?> deleteStudentPicture(@PathVariable String studentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            studentService.deleteStudentPicture(studentId);
            response.put("success", true);
            response.put("message", "Profile picture deleted successfully");
            log.info("Profile picture deleted successfully for student: {}", studentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error deleting picture for student {}: {}", studentId, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{studentId}/profile-picture")
    public ResponseEntity<byte[]> getStudentProfilePicture(@PathVariable String studentId) {
        return profilePictureService.getProfilePicture(studentId)
                .map(pic -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(pic.getContentType()))
                        .body(pic.getImageData()))
                .orElse(ResponseEntity.notFound().build());
    }
}
