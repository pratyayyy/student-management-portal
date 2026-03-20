package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.RegistrationRequest;
import com.ija.student_management_portal.service.UserCredentialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class LoginController {

    @Autowired
    private UserCredentialService userCredentialService;

    /**
     * Handle root path - redirect to login if not authenticated, otherwise redirect to appropriate home
     */
    @GetMapping("/")
    public String handleRoot() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If not authenticated, show login page
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        // If authenticated, redirect based on role
        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        boolean isStudent = authentication.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"));

        if (isAdmin) {
            return "redirect:/home";
        } else if (isStudent) {
            return "redirect:/student/home";
        }

        return "redirect:/login";
    }

    /**
     * Show login page
     */
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully");
        }
        return "login";
    }

    /**
     * Handle successful login - Spring Security will call this after successful authentication
     * This endpoint is configured in SecurityConfig.formLogin().defaultSuccessUrl()
     */
    @GetMapping("/login-success")
    public String loginSuccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        boolean isStudent = authentication.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"));

        if (isAdmin) {
            log.info("Admin user {} successfully authenticated", authentication.getName());
            return "redirect:/home";
        } else if (isStudent) {
            log.info("Student user {} successfully authenticated", authentication.getName());
            return "redirect:/student/home";
        }

        log.warn("User {} authenticated but has no recognized role", authentication.getName());
        return "redirect:/login";
    }

    /**
     * Show registration page
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "login";
    }

    /**
     * Process user registration
     */
    @PostMapping("/register")
    public String processRegistration(@ModelAttribute RegistrationRequest registrationRequest,
                                     RedirectAttributes redirectAttributes) {
        try {
            log.info("Registration attempt for username: {}", registrationRequest.getUsername());

            // Validate passwords match
            if (!registrationRequest.getPassword().equals(registrationRequest.getConfirmPassword())) {
                log.warn("Passwords do not match for username: {}", registrationRequest.getUsername());
                redirectAttributes.addAttribute("regError", "Passwords do not match");
                return "redirect:/login";
            }

            // Validate username length
            if (registrationRequest.getUsername().length() < 3) {
                log.warn("Username too short: {}", registrationRequest.getUsername());
                redirectAttributes.addAttribute("regError", "Username must be at least 3 characters");
                return "redirect:/login";
            }

            // Validate password length
            if (registrationRequest.getPassword().length() < 6) {
                log.warn("Password too short for username: {}", registrationRequest.getUsername());
                redirectAttributes.addAttribute("regError", "Password must be at least 6 characters");
                return "redirect:/login";
            }

            // Validate role
            if (registrationRequest.getRole() == null || registrationRequest.getRole().isEmpty()) {
                log.warn("No role selected for username: {}", registrationRequest.getUsername());
                redirectAttributes.addAttribute("regError", "Please select a role");
                return "redirect:/login";
            }

            // Validate student role has student ID
            if ("STUDENT".equals(registrationRequest.getRole())) {
                if (registrationRequest.getStudentId() == null || registrationRequest.getStudentId().isEmpty()) {
                    log.warn("Student ID required for STUDENT role: {}", registrationRequest.getUsername());
                    redirectAttributes.addAttribute("regError", "Student ID is required for Student role");
                    return "redirect:/login";
                }
            }

            // Create credentials based on role
            if ("ADMIN".equals(registrationRequest.getRole())) {
                var result = userCredentialService.createAdminCredentials(
                    registrationRequest.getUsername(),
                    registrationRequest.getPassword()
                );

                if (result.isEmpty()) {
                    log.warn("Username already exists: {}", registrationRequest.getUsername());
                    redirectAttributes.addAttribute("regError", "Username already exists");
                    return "redirect:/login";
                }

                log.info("Admin account created successfully: {}", registrationRequest.getUsername());
            } else if ("STUDENT".equals(registrationRequest.getRole())) {
                var result = userCredentialService.createStudentCredentials(
                    registrationRequest.getUsername(),
                    registrationRequest.getPassword(),
                    registrationRequest.getStudentId()
                );

                if (result.isEmpty()) {
                    log.warn("Failed to create student credentials - username/studentId may already exist: {}", registrationRequest.getUsername());
                    redirectAttributes.addAttribute("regError", "Username or Student ID already exists");
                    return "redirect:/login";
                }

                log.info("Student account created successfully: {}", registrationRequest.getUsername());
            }

            // Redirect to login with success message
            redirectAttributes.addAttribute("registered", "true");
            return "redirect:/login";

        } catch (Exception e) {
            log.error("Error during registration for username: {}", registrationRequest.getUsername(), e);
            redirectAttributes.addAttribute("regError", "An error occurred during registration. Please try again.");
            return "redirect:/login";
        }
    }
}
