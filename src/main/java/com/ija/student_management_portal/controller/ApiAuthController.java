package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.RegistrationRequest;
import com.ija.student_management_portal.service.UserCredentialService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class ApiAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserCredentialService userCredentialService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        String username = body.get("username");
        String password = body.get("password");
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(auth);

            var repo = new HttpSessionSecurityContextRepository();
            repo.saveContext(SecurityContextHolder.getContext(), request, response);

            return ResponseEntity.ok(buildUserMap(auth));
        } catch (BadCredentialsException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Invalid username or password");
            return ResponseEntity.status(401).body(err);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }
        return ResponseEntity.ok(buildUserMap(auth));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest req) {
        try {
            if (!req.getPassword().equals(req.getConfirmPassword()))
                return ResponseEntity.badRequest().body(Map.of("message", "Passwords do not match"));
            if (req.getUsername().length() < 3)
                return ResponseEntity.badRequest().body(Map.of("message", "Username must be at least 3 characters"));
            if (req.getPassword().length() < 6)
                return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));

            if ("ADMIN".equals(req.getRole())) {
                var result = userCredentialService.createAdminCredentials(req.getUsername(), req.getPassword());
                if (result.isEmpty())
                    return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));
            } else if ("STUDENT".equals(req.getRole())) {
                if (req.getStudentId() == null || req.getStudentId().isEmpty())
                    return ResponseEntity.badRequest().body(Map.of("message", "Student ID required"));
                var result = userCredentialService.createStudentCredentials(req.getUsername(), req.getPassword(), req.getStudentId());
                if (result.isEmpty())
                    return ResponseEntity.badRequest().body(Map.of("message", "Username or Student ID already exists"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid role"));
            }
            return ResponseEntity.ok(Map.of("message", "Account created successfully"));
        } catch (Exception e) {
            log.error("Registration error", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Registration failed"));
        }
    }

    private Map<String, Object> buildUserMap(Authentication auth) {
        Map<String, Object> map = new HashMap<>();
        map.put("username", auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        map.put("role", isAdmin ? "ADMIN" : "STUDENT");

        if (!isAdmin) {
            try {
                var cred = userCredentialService.getUserByUsername(auth.getName());
                cred.ifPresent(c -> map.put("studentId", c.getStudentId()));
            } catch (Exception ignored) {}
        }
        return map;
    }
}
