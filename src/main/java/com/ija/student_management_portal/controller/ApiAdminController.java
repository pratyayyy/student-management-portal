package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.service.StudentService;
import com.ija.student_management_portal.service.UserCredentialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class ApiAdminController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserCredentialService userCredentialService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            long totalStudents = studentService.getAllStudents().size();
            long registeredUsers = userCredentialService.countAll();
            long studentAccounts = userCredentialService.countByRole("STUDENT");
            stats.put("totalStudents", totalStudents);
            stats.put("registeredUsers", registeredUsers);
            stats.put("studentAccounts", studentAccounts);
        } catch (Exception e) {
            log.error("Error getting stats", e);
        }
        return ResponseEntity.ok(stats);
    }
}
