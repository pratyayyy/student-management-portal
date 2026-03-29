package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@Slf4j
public class ApiStudentController {

    @Autowired
    private StudentService studentService;

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
}
