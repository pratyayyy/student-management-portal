package com.ija.student_management_portal.repository;

import com.ija.student_management_portal.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("StudentRepository Tests")
public class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Student student;

    @BeforeEach
    public void setUp() {
        student = Student.builder()
                .name("John Doe")
                .phoneNumber("1234567890")
                .alternateNumber("0987654321")
                .standard("10")
                .address("123 Main St")
                .guardiansName("Jane Doe")
                .admissionDate(LocalDateTime.now())
                .studentId("2025-0001")
                .build();
    }

    @Test
    @DisplayName("Should save student successfully")
    public void testSaveStudent_Success() {
        // Act
        Student savedStudent = studentRepository.save(student);
        testEntityManager.flush();

        // Assert
        assertNotNull(savedStudent.getId());
        assertEquals("John Doe", savedStudent.getName());
        assertEquals("2025-0001", savedStudent.getStudentId());
    }

    @Test
    @DisplayName("Should find student by student ID")
    public void testFindStudentByStudentId_Success() {
        // Arrange
        testEntityManager.persistAndFlush(student);

        // Act
        Optional<Student> foundStudent = studentRepository.findStudentByStudentId("2025-0001");

        // Assert
        assertTrue(foundStudent.isPresent());
        assertEquals("John Doe", foundStudent.get().getName());
        assertEquals("1234567890", foundStudent.get().getPhoneNumber());
    }

    @Test
    @DisplayName("Should return empty when student ID not found")
    public void testFindStudentByStudentId_NotFound() {
        // Act
        Optional<Student> foundStudent = studentRepository.findStudentByStudentId("2025-9999");

        // Assert
        assertTrue(foundStudent.isEmpty());
    }

    @Test
    @DisplayName("Should delete student by student ID")
    public void testDeleteByStudentId_Success() {
        // Arrange
        testEntityManager.persistAndFlush(student);

        // Act
        studentRepository.deleteByStudentId("2025-0001");
        testEntityManager.flush();

        // Assert
        Optional<Student> deletedStudent = studentRepository.findStudentByStudentId("2025-0001");
        assertTrue(deletedStudent.isEmpty());
    }

    @Test
    @DisplayName("Should find all students")
    public void testFindAll_Success() {
        // Arrange
        Student student2 = Student.builder()
                .name("Jane Smith")
                .phoneNumber("9876543210")
                .alternateNumber("1234567890")
                .standard("11")
                .address("456 Oak St")
                .guardiansName("John Smith")
                .admissionDate(LocalDateTime.now())
                .studentId("2025-0002")
                .build();

        testEntityManager.persistAndFlush(student);
        testEntityManager.persistAndFlush(student2);

        // Act
        List<Student> students = studentRepository.findAll();

        // Assert
        assertNotNull(students);
        assertEquals(2, students.size());
    }

    @Test
    @DisplayName("Should find all students when empty")
    public void testFindAll_Empty() {
        // Act
        List<Student> students = studentRepository.findAll();

        // Assert
        assertNotNull(students);
        assertTrue(students.isEmpty());
    }

    @Test
    @DisplayName("Should update student successfully")
    public void testUpdateStudent_Success() {
        // Arrange
        testEntityManager.persistAndFlush(student);

        // Act
        Optional<Student> foundStudent = studentRepository.findStudentByStudentId("2025-0001");
        if (foundStudent.isPresent()) {
            Student updateStudent = foundStudent.get();
            updateStudent.setName("Jane Doe");
            updateStudent.setPhoneNumber("5555555555");
            studentRepository.save(updateStudent);
            testEntityManager.flush();
        }

        // Assert
        Optional<Student> updatedStudent = studentRepository.findStudentByStudentId("2025-0001");
        assertTrue(updatedStudent.isPresent());
        assertEquals("Jane Doe", updatedStudent.get().getName());
        assertEquals("5555555555", updatedStudent.get().getPhoneNumber());
    }

    @Test
    @DisplayName("Should enforce unique constraint on student ID")
    public void testUniqueConstraintOnStudentId() {
        // Arrange
        testEntityManager.persistAndFlush(student);

        Student duplicateStudent = Student.builder()
                .name("Duplicate Name")
                .phoneNumber("1111111111")
                .alternateNumber("2222222222")
                .standard("10")
                .address("789 Pine St")
                .guardiansName("Guardian")
                .admissionDate(LocalDateTime.now())
                .studentId("2025-0001")  // Same student ID
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            testEntityManager.persistAndFlush(duplicateStudent);
        });
    }

    @Test
    @DisplayName("Should find student by ID")
    public void testFindById_Success() {
        // Arrange
        Student savedStudent = testEntityManager.persistAndFlush(student);
        Long studentId = savedStudent.getId();

        // Act
        Optional<Student> foundStudent = studentRepository.findById(studentId);

        // Assert
        assertTrue(foundStudent.isPresent());
        assertEquals("John Doe", foundStudent.get().getName());
    }
}
