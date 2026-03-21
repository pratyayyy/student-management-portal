package com.ija.student_management_portal.service;

import com.ija.student_management_portal.entity.UserCredential;
import com.ija.student_management_portal.repository.UserCredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCredentialService Tests")
public class UserCredentialServiceTest {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserCredentialService userCredentialService;

    private UserCredential userCredential;

    @BeforeEach
    public void setUp() {
        userCredential = UserCredential.builder()
                .id(1L)
                .username("student@test.com")
                .password("encodedPassword123")
                .studentId("2025-0001")
                .role(UserCredential.UserRole.STUDENT)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should create student credentials successfully")
    public void testCreateStudentCredentials_Success() {
        // Arrange
        String username = "student@test.com";
        String password = "password123";
        String studentId = "2025-0001";
        String encodedPassword = "encodedPassword123";

        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.empty());
        when(userCredentialRepository.findByStudentId(studentId))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(password))
                .thenReturn(encodedPassword);
        when(userCredentialRepository.save(any(UserCredential.class)))
                .thenReturn(userCredential);

        // Act
        Optional<UserCredential> result = userCredentialService.createStudentCredentials(username, password, studentId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("student@test.com", result.get().getUsername());
        assertEquals(UserCredential.UserRole.STUDENT, result.get().getRole());
        assertTrue(result.get().isEnabled());
        verify(userCredentialRepository, times(1)).save(any(UserCredential.class));
        verify(passwordEncoder, times(1)).encode(password);
    }

    @Test
    @DisplayName("Should not create student credentials when username already exists")
    public void testCreateStudentCredentials_UsernameExists() {
        // Arrange
        String username = "student@test.com";
        String password = "password123";
        String studentId = "2025-0001";

        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.of(userCredential));

        // Act
        Optional<UserCredential> result = userCredentialService.createStudentCredentials(username, password, studentId);

        // Assert
        assertTrue(result.isEmpty());
        verify(userCredentialRepository, never()).save(any(UserCredential.class));
    }

    @Test
    @DisplayName("Should not create student credentials when student ID already has account")
    public void testCreateStudentCredentials_StudentIdExists() {
        // Arrange
        String username = "newstudent@test.com";
        String password = "password123";
        String studentId = "2025-0001";

        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.empty());
        when(userCredentialRepository.findByStudentId(studentId))
                .thenReturn(Optional.of(userCredential));

        // Act
        Optional<UserCredential> result = userCredentialService.createStudentCredentials(username, password, studentId);

        // Assert
        assertTrue(result.isEmpty());
        verify(userCredentialRepository, never()).save(any(UserCredential.class));
    }

    @Test
    @DisplayName("Should create admin credentials successfully")
    public void testCreateAdminCredentials_Success() {
        // Arrange
        String username = "admin@test.com";
        String password = "adminPassword123";
        String encodedPassword = "encodedAdminPassword123";

        UserCredential adminCredential = UserCredential.builder()
                .id(2L)
                .username(username)
                .password(encodedPassword)
                .role(UserCredential.UserRole.ADMIN)
                .enabled(true)
                .build();

        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(password))
                .thenReturn(encodedPassword);
        when(userCredentialRepository.save(any(UserCredential.class)))
                .thenReturn(adminCredential);

        // Act
        Optional<UserCredential> result = userCredentialService.createAdminCredentials(username, password);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("admin@test.com", result.get().getUsername());
        assertEquals(UserCredential.UserRole.ADMIN, result.get().getRole());
        assertTrue(result.get().isEnabled());
        verify(userCredentialRepository, times(1)).save(any(UserCredential.class));
        verify(passwordEncoder, times(1)).encode(password);
    }

    @Test
    @DisplayName("Should not create admin credentials when username exists")
    public void testCreateAdminCredentials_UsernameExists() {
        // Arrange
        String username = "admin@test.com";
        String password = "adminPassword123";

        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.of(userCredential));

        // Act
        Optional<UserCredential> result = userCredentialService.createAdminCredentials(username, password);

        // Assert
        assertTrue(result.isEmpty());
        verify(userCredentialRepository, never()).save(any(UserCredential.class));
    }

    @Test
    @DisplayName("Should get user by username")
    public void testGetUserByUsername_Success() {
        // Arrange
        String username = "student@test.com";
        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.of(userCredential));

        // Act
        Optional<UserCredential> result = userCredentialService.getUserByUsername(username);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(userCredentialRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should return empty when user not found by username")
    public void testGetUserByUsername_NotFound() {
        // Arrange
        String username = "nonexistent@test.com";
        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        // Act
        Optional<UserCredential> result = userCredentialService.getUserByUsername(username);

        // Assert
        assertTrue(result.isEmpty());
        verify(userCredentialRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should get user by student ID")
    public void testGetUserByStudentId_Success() {
        // Arrange
        String studentId = "2025-0001";
        when(userCredentialRepository.findByStudentId(studentId))
                .thenReturn(Optional.of(userCredential));

        // Act
        Optional<UserCredential> result = userCredentialService.getUserByStudentId(studentId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(studentId, result.get().getStudentId());
        verify(userCredentialRepository, times(1)).findByStudentId(studentId);
    }

    @Test
    @DisplayName("Should update password successfully")
    public void testUpdatePassword_Success() {
        // Arrange
        String username = "student@test.com";
        String newPassword = "newPassword456";
        String encodedNewPassword = "encodedNewPassword456";

        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.of(userCredential));
        when(passwordEncoder.encode(newPassword))
                .thenReturn(encodedNewPassword);
        when(userCredentialRepository.save(any(UserCredential.class)))
                .thenReturn(userCredential);

        // Act
        boolean result = userCredentialService.updatePassword(username, newPassword);

        // Assert
        assertTrue(result);
        verify(userCredentialRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userCredentialRepository, times(1)).save(any(UserCredential.class));
    }

    @Test
    @DisplayName("Should return false when updating password for non-existent user")
    public void testUpdatePassword_UserNotFound() {
        // Arrange
        String username = "nonexistent@test.com";
        String newPassword = "newPassword456";

        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        // Act
        boolean result = userCredentialService.updatePassword(username, newPassword);

        // Assert
        assertFalse(result);
        verify(userCredentialRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userCredentialRepository, never()).save(any(UserCredential.class));
    }

    @Test
    @DisplayName("Should disable user successfully")
    public void testDisableUser_Success() {
        // Arrange
        String username = "student@test.com";
        UserCredential disabledUser = UserCredential.builder()
                .id(1L)
                .username(username)
                .password("encodedPassword123")
                .studentId("2025-0001")
                .role(UserCredential.UserRole.STUDENT)
                .enabled(false)
                .build();

        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.of(userCredential));
        when(userCredentialRepository.save(any(UserCredential.class)))
                .thenReturn(disabledUser);

        // Act
        boolean result = userCredentialService.disableUser(username);

        // Assert
        assertTrue(result);
        verify(userCredentialRepository, times(1)).findByUsername(username);
        verify(userCredentialRepository, times(1)).save(any(UserCredential.class));
    }

    @Test
    @DisplayName("Should return false when disabling non-existent user")
    public void testDisableUser_UserNotFound() {
        // Arrange
        String username = "nonexistent@test.com";

        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        // Act
        boolean result = userCredentialService.disableUser(username);

        // Assert
        assertFalse(result);
        verify(userCredentialRepository, times(1)).findByUsername(username);
        verify(userCredentialRepository, never()).save(any(UserCredential.class));
    }
}
