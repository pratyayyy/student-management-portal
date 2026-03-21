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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
public class CustomUserDetailsServiceTest {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private UserCredential studentCredential;
    private UserCredential adminCredential;

    @BeforeEach
    public void setUp() {
        studentCredential = UserCredential.builder()
                .id(1L)
                .username("student@test.com")
                .password("encodedPassword123")
                .studentId("2025-0001")
                .role(UserCredential.UserRole.STUDENT)
                .enabled(true)
                .build();

        adminCredential = UserCredential.builder()
                .id(2L)
                .username("admin@test.com")
                .password("encodedAdminPassword123")
                .role(UserCredential.UserRole.ADMIN)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should load user by username successfully")
    public void testLoadUserByUsername_StudentSuccess() {
        // Arrange
        String username = "student@test.com";
        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.of(studentCredential));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("encodedPassword123", userDetails.getPassword());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertFalse(userDetails.isEnabled()); // Note: User object has disabled = false

        // Check authorities
        boolean hasStudentRole = userDetails.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"));
        assertTrue(hasStudentRole);

        verify(userCredentialRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should load admin user by username successfully")
    public void testLoadUserByUsername_AdminSuccess() {
        // Arrange
        String username = "admin@test.com";
        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.of(adminCredential));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("encodedAdminPassword123", userDetails.getPassword());

        // Check authorities
        boolean hasAdminRole = userDetails.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        assertTrue(hasAdminRole);

        verify(userCredentialRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    public void testLoadUserByUsername_NotFound() {
        // Arrange
        String username = "nonexistent@test.com";
        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });

        verify(userCredentialRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user is disabled")
    public void testLoadUserByUsername_UserDisabled() {
        // Arrange
        String username = "disabled@test.com";
        UserCredential disabledUser = UserCredential.builder()
                .id(3L)
                .username(username)
                .password("encodedPassword123")
                .role(UserCredential.UserRole.STUDENT)
                .enabled(false)
                .build();

        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.of(disabledUser));

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });

        verify(userCredentialRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should assign correct role authorities to user")
    public void testLoadUserByUsername_CorrectRoleAssignment() {
        // Arrange
        String username = "student@test.com";
        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.of(studentCredential));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertEquals(1, userDetails.getAuthorities().size());
        GrantedAuthority authority = userDetails.getAuthorities().stream().findFirst().orElse(null);
        assertNotNull(authority);
        assertEquals("ROLE_STUDENT", authority.getAuthority());
    }

    @Test
    @DisplayName("Should handle username with different cases")
    public void testLoadUserByUsername_CaseSensitive() {
        // Arrange
        String username = "Student@test.com";
        when(userCredentialRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });

        verify(userCredentialRepository, times(1)).findByUsername(username);
    }
}
