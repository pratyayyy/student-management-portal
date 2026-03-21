package com.ija.student_management_portal.repository;

import com.ija.student_management_portal.entity.UserCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("UserCredentialRepository Tests")
public class UserCredentialRepositoryTest {

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private UserCredential userCredential;

    @BeforeEach
    public void setUp() {
        userCredential = UserCredential.builder()
                .username("student@test.com")
                .password("encodedPassword123")
                .studentId("2025-0001")
                .role(UserCredential.UserRole.STUDENT)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should save user credential successfully")
    public void testSaveUserCredential_Success() {
        // Act
        UserCredential savedCredential = userCredentialRepository.save(userCredential);
        testEntityManager.flush();

        // Assert
        assertNotNull(savedCredential.getId());
        assertEquals("student@test.com", savedCredential.getUsername());
        assertEquals("2025-0001", savedCredential.getStudentId());
        assertTrue(savedCredential.isEnabled());
    }

    @Test
    @DisplayName("Should find user by username")
    public void testFindByUsername_Success() {
        // Arrange
        testEntityManager.persistAndFlush(userCredential);

        // Act
        Optional<UserCredential> foundUser = userCredentialRepository.findByUsername("student@test.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("student@test.com", foundUser.get().getUsername());
        assertEquals(UserCredential.UserRole.STUDENT, foundUser.get().getRole());
    }

    @Test
    @DisplayName("Should return empty when username not found")
    public void testFindByUsername_NotFound() {
        // Act
        Optional<UserCredential> foundUser = userCredentialRepository.findByUsername("nonexistent@test.com");

        // Assert
        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("Should find user by student ID")
    public void testFindByStudentId_Success() {
        // Arrange
        testEntityManager.persistAndFlush(userCredential);

        // Act
        Optional<UserCredential> foundUser = userCredentialRepository.findByStudentId("2025-0001");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("student@test.com", foundUser.get().getUsername());
        assertEquals("2025-0001", foundUser.get().getStudentId());
    }

    @Test
    @DisplayName("Should return empty when student ID not found")
    public void testFindByStudentId_NotFound() {
        // Act
        Optional<UserCredential> foundUser = userCredentialRepository.findByStudentId("2025-9999");

        // Assert
        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("Should enforce unique constraint on username")
    public void testUniqueConstraintOnUsername() {
        // Arrange
        testEntityManager.persistAndFlush(userCredential);

        UserCredential duplicateUser = UserCredential.builder()
                .username("student@test.com")  // Same username
                .password("differentPassword")
                .studentId("2025-0002")
                .role(UserCredential.UserRole.STUDENT)
                .enabled(true)
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            testEntityManager.persistAndFlush(duplicateUser);
        });
    }

    @Test
    @DisplayName("Should enforce unique constraint on student ID")
    public void testUniqueConstraintOnStudentId() {
        // Arrange
        testEntityManager.persistAndFlush(userCredential);

        UserCredential duplicateStudent = UserCredential.builder()
                .username("different@test.com")
                .password("encodedPassword123")
                .studentId("2025-0001")  // Same student ID
                .role(UserCredential.UserRole.STUDENT)
                .enabled(true)
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            testEntityManager.persistAndFlush(duplicateStudent);
        });
    }

    @Test
    @DisplayName("Should save admin user without student ID")
    public void testSaveAdminUser_Success() {
        // Arrange
        UserCredential adminUser = UserCredential.builder()
                .username("admin@test.com")
                .password("encodedAdminPassword")
                .role(UserCredential.UserRole.ADMIN)
                .enabled(true)
                .build();

        // Act
        UserCredential savedAdmin = userCredentialRepository.save(adminUser);
        testEntityManager.flush();

        // Assert
        assertNotNull(savedAdmin.getId());
        assertEquals("admin@test.com", savedAdmin.getUsername());
        assertNull(savedAdmin.getStudentId());
        assertEquals(UserCredential.UserRole.ADMIN, savedAdmin.getRole());
    }

    @Test
    @DisplayName("Should update user credential successfully")
    public void testUpdateUserCredential_Success() {
        // Arrange
        UserCredential savedUser = testEntityManager.persistAndFlush(userCredential);

        // Act
        savedUser.setPassword("newEncodedPassword");
        savedUser.setEnabled(false);
        UserCredential updatedUser = userCredentialRepository.save(savedUser);
        testEntityManager.flush();

        // Assert
        assertEquals("newEncodedPassword", updatedUser.getPassword());
        assertFalse(updatedUser.isEnabled());
    }

    @Test
    @DisplayName("Should delete user credential successfully")
    public void testDeleteUserCredential_Success() {
        // Arrange
        UserCredential savedUser = testEntityManager.persistAndFlush(userCredential);
        Long userId = savedUser.getId();

        // Act
        userCredentialRepository.deleteById(userId);
        testEntityManager.flush();

        // Assert
        Optional<UserCredential> deletedUser = userCredentialRepository.findById(userId);
        assertTrue(deletedUser.isEmpty());
    }

    @Test
    @DisplayName("Should find user by ID")
    public void testFindById_Success() {
        // Arrange
        UserCredential savedUser = testEntityManager.persistAndFlush(userCredential);

        // Act
        Optional<UserCredential> foundUser = userCredentialRepository.findById(savedUser.getId());

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("student@test.com", foundUser.get().getUsername());
    }

    @Test
    @DisplayName("Should handle multiple users with different roles")
    public void testMultipleUsersWithDifferentRoles() {
        // Arrange
        UserCredential student1 = UserCredential.builder()
                .username("student1@test.com")
                .password("password1")
                .studentId("2025-0001")
                .role(UserCredential.UserRole.STUDENT)
                .enabled(true)
                .build();

        UserCredential student2 = UserCredential.builder()
                .username("student2@test.com")
                .password("password2")
                .studentId("2025-0002")
                .role(UserCredential.UserRole.STUDENT)
                .enabled(true)
                .build();

        UserCredential admin = UserCredential.builder()
                .username("admin@test.com")
                .password("adminPassword")
                .role(UserCredential.UserRole.ADMIN)
                .enabled(true)
                .build();

        // Act
        testEntityManager.persistAndFlush(student1);
        testEntityManager.persistAndFlush(student2);
        testEntityManager.persistAndFlush(admin);

        // Assert
        Optional<UserCredential> foundStudent1 = userCredentialRepository.findByUsername("student1@test.com");
        Optional<UserCredential> foundStudent2 = userCredentialRepository.findByUsername("student2@test.com");
        Optional<UserCredential> foundAdmin = userCredentialRepository.findByUsername("admin@test.com");

        assertTrue(foundStudent1.isPresent());
        assertTrue(foundStudent2.isPresent());
        assertTrue(foundAdmin.isPresent());
        assertEquals(UserCredential.UserRole.STUDENT, foundStudent1.get().getRole());
        assertEquals(UserCredential.UserRole.ADMIN, foundAdmin.get().getRole());
    }
}
