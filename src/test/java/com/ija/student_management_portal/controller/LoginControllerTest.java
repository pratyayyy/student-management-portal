package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.RegistrationRequest;
import com.ija.student_management_portal.entity.UserCredential;
import com.ija.student_management_portal.service.UserCredentialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginController Tests")
public class LoginControllerTest {

    @Mock
    private UserCredentialService userCredentialService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private LoginController loginController;

    private RegistrationRequest registrationRequest;
    private UserCredential userCredential;

    @BeforeEach
    public void setUp() {
        registrationRequest = new RegistrationRequest();
        registrationRequest.setUsername("testuser@test.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setConfirmPassword("password123");
        registrationRequest.setRole("STUDENT");
        registrationRequest.setStudentId("2025-0001");

        userCredential = UserCredential.builder()
                .id(1L)
                .username("testuser@test.com")
                .password("encodedPassword123")
                .studentId("2025-0001")
                .role(UserCredential.UserRole.STUDENT)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should show login page")
    public void testShowLoginPage_NoParams() {
        // Act
        String result = loginController.showLoginPage(null, null, model);

        // Assert
        assertEquals("login", result);
        verify(model, never()).addAttribute(anyString(), anyString());
    }

    @Test
    @DisplayName("Should show login page with error message")
    public void testShowLoginPage_WithError() {
        // Act
        String result = loginController.showLoginPage("1", null, model);

        // Assert
        assertEquals("login", result);
        verify(model, times(1)).addAttribute("errorMessage", "Invalid username or password");
    }

    @Test
    @DisplayName("Should show login page with logout message")
    public void testShowLoginPage_WithLogout() {
        // Act
        String result = loginController.showLoginPage(null, "1", model);

        // Assert
        assertEquals("login", result);
        verify(model, times(1)).addAttribute("logoutMessage", "You have been logged out successfully");
    }

    @Test
    @DisplayName("Should show register page with new registration request")
    public void testShowRegisterPage_Success() {
        // Act
        String result = loginController.showRegisterPage(model);

        // Assert
        assertEquals("login", result);
        verify(model, times(1)).addAttribute(anyString(), any(RegistrationRequest.class));
    }

    @Test
    @DisplayName("Should successfully register student user")
    public void testProcessRegistration_StudentSuccess() {
        // Arrange
        when(userCredentialService.createStudentCredentials(
                registrationRequest.getUsername(),
                registrationRequest.getPassword(),
                registrationRequest.getStudentId()))
                .thenReturn(Optional.of(userCredential));

        // Act
        String result = loginController.processRegistration(registrationRequest, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
        verify(userCredentialService, times(1)).createStudentCredentials(
                registrationRequest.getUsername(),
                registrationRequest.getPassword(),
                registrationRequest.getStudentId());
        verify(redirectAttributes, times(1)).addAttribute("registered", "true");
    }

    @Test
    @DisplayName("Should successfully register admin user")
    public void testProcessRegistration_AdminSuccess() {
        // Arrange
        registrationRequest.setRole("ADMIN");
        registrationRequest.setStudentId(null);

        when(userCredentialService.createAdminCredentials(
                registrationRequest.getUsername(),
                registrationRequest.getPassword()))
                .thenReturn(Optional.of(userCredential));

        // Act
        String result = loginController.processRegistration(registrationRequest, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
        verify(userCredentialService, times(1)).createAdminCredentials(
                registrationRequest.getUsername(),
                registrationRequest.getPassword());
        verify(redirectAttributes, times(1)).addAttribute("registered", "true");
    }

    @Test
    @DisplayName("Should reject registration when passwords do not match")
    public void testProcessRegistration_PasswordMismatch() {
        // Arrange
        registrationRequest.setConfirmPassword("differentPassword");

        // Act
        String result = loginController.processRegistration(registrationRequest, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
        verify(redirectAttributes, times(1)).addAttribute("regError", "Passwords do not match");
        verify(userCredentialService, never()).createStudentCredentials(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should reject registration when username is too short")
    public void testProcessRegistration_UsernameTooShort() {
        // Arrange
        registrationRequest.setUsername("ab");

        // Act
        String result = loginController.processRegistration(registrationRequest, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
        verify(redirectAttributes, times(1)).addAttribute("regError", "Username must be at least 3 characters");
        verify(userCredentialService, never()).createStudentCredentials(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should reject registration when password is too short")
    public void testProcessRegistration_PasswordTooShort() {
        // Arrange
        registrationRequest.setPassword("pass1");
        registrationRequest.setConfirmPassword("pass1");

        // Act
        String result = loginController.processRegistration(registrationRequest, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
        verify(redirectAttributes, times(1)).addAttribute("regError", "Password must be at least 6 characters");
        verify(userCredentialService, never()).createStudentCredentials(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should reject registration when role is not selected")
    public void testProcessRegistration_NoRoleSelected() {
        // Arrange
        registrationRequest.setRole("");

        // Act
        String result = loginController.processRegistration(registrationRequest, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
        verify(redirectAttributes, times(1)).addAttribute("regError", "Please select a role");
        verify(userCredentialService, never()).createStudentCredentials(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should reject student registration without student ID")
    public void testProcessRegistration_StudentWithoutStudentId() {
        // Arrange
        registrationRequest.setStudentId("");

        // Act
        String result = loginController.processRegistration(registrationRequest, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
        verify(redirectAttributes, times(1)).addAttribute("regError", "Student ID is required for Student role");
        verify(userCredentialService, never()).createStudentCredentials(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should reject registration when username already exists")
    public void testProcessRegistration_UsernameAlreadyExists() {
        // Arrange
        when(userCredentialService.createStudentCredentials(
                registrationRequest.getUsername(),
                registrationRequest.getPassword(),
                registrationRequest.getStudentId()))
                .thenReturn(Optional.empty());

        // Act
        String result = loginController.processRegistration(registrationRequest, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
        verify(redirectAttributes, times(1)).addAttribute("regError", "Username or Student ID already exists");
    }

    @Test
    @DisplayName("Should handle exception during registration")
    public void testProcessRegistration_ExceptionHandling() {
        // Arrange
        when(userCredentialService.createStudentCredentials(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        String result = loginController.processRegistration(registrationRequest, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
        verify(redirectAttributes, times(1)).addAttribute(
                eq("regError"),
                argThat(message -> message.toString().contains("An error occurred during registration"))
        );
    }

    @Test
    @DisplayName("Should reject registration when student ID is null for student role")
    public void testProcessRegistration_StudentIdNull() {
        // Arrange
        registrationRequest.setStudentId(null);

        // Act
        String result = loginController.processRegistration(registrationRequest, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", result);
        verify(redirectAttributes, times(1)).addAttribute("regError", "Student ID is required for Student role");
    }
}
