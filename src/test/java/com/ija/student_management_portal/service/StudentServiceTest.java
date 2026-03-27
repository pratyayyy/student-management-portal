package com.ija.student_management_portal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.entity.Student;
import com.ija.student_management_portal.repository.StudentRepository;
import com.ija.student_management_portal.service.ProfilePictureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Tests")
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ProfilePictureService profilePictureService;

    @Mock
    private ObjectMapper objectMapper;

    private StudentService studentService;

    private StudentDTO studentDTO;
    private Student student;

    @BeforeEach
    public void setUp() {
        studentService = new StudentService(studentRepository);
        ReflectionTestUtils.setField(studentService, "objectmapper", objectMapper);
        ReflectionTestUtils.setField(studentService, "profilePictureService", profilePictureService);

        // Initialize test data
        studentDTO = new StudentDTO();
        studentDTO.setName("John Doe");
        studentDTO.setPhoneNumber("1234567890");
        studentDTO.setAlternateNumber("0987654321");
        studentDTO.setStandard("10");
        studentDTO.setAddress("123 Main St");
        studentDTO.setGuardiansName("Jane Doe");
        studentDTO.setStudentId("2025-0001");

        student = Student.builder()
                .id(1L)
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
    @DisplayName("Should save student successfully with manually provided student ID")
    public void testSaveStudent_Success() {
        // Arrange
        when(studentRepository.findStudentByStudentId("2025-0001"))
                .thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class)))
                .thenReturn(student);
        when(objectMapper.convertValue(student, StudentDTO.class))
                .thenReturn(studentDTO);

        // Act
        Optional<StudentDTO> result = studentService.saveStudent(studentDTO);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        assertEquals("2025-0001", result.get().getStudentId());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    @DisplayName("Should throw exception when student ID is missing")
    public void testSaveStudent_MissingStudentId() {
        // Arrange
        studentDTO.setStudentId(null);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.saveStudent(studentDTO));
        assertEquals("Student ID is required", ex.getMessage());
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when student ID is blank")
    public void testSaveStudent_BlankStudentId() {
        // Arrange
        studentDTO.setStudentId("   ");

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.saveStudent(studentDTO));
        assertEquals("Student ID is required", ex.getMessage());
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when student ID already exists")
    public void testSaveStudent_DuplicateStudentId() {
        // Arrange
        when(studentRepository.findStudentByStudentId("2025-0001"))
                .thenReturn(Optional.of(student));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.saveStudent(studentDTO));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve student by student ID")
    public void testGetStudentById_Success() {
        // Arrange
        String studentId = "2025-0001";
        when(studentRepository.findStudentByStudentId(studentId))
                .thenReturn(Optional.of(student));
        when(objectMapper.convertValue(Optional.of(student), StudentDTO.class))
                .thenReturn(studentDTO);

        // Act
        Optional<StudentDTO> result = studentService.getStudentById(studentId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(studentRepository, times(1)).findStudentByStudentId(studentId);
    }

    @Test
    @DisplayName("Should update student successfully")
    public void testUpdateStudent_Success() {
        // Arrange
        String studentId = "2025-0001";
        StudentDTO updatedDTO = new StudentDTO();
        updatedDTO.setName("Jane Smith");
        updatedDTO.setPhoneNumber("5555555555");
        updatedDTO.setAlternateNumber("4444444444");
        updatedDTO.setStandard("11");
        updatedDTO.setAddress("456 Oak St");
        updatedDTO.setGuardiansName("John Smith");

        Student existingStudent = student;
        existingStudent.setName("Jane Smith");
        existingStudent.setStandard("11");
        existingStudent.setPhoneNumber("5555555555");
        existingStudent.setAlternateNumber("4444444444");
        existingStudent.setAddress("456 Oak St");
        existingStudent.setGuardiansName("John Smith");

        when(studentRepository.findStudentByStudentId(studentId))
                .thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class)))
                .thenReturn(existingStudent);
        when(objectMapper.convertValue(existingStudent, StudentDTO.class))
                .thenReturn(updatedDTO);

        // Act
        Optional<StudentDTO> result = studentService.updateStudent(studentId, updatedDTO);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Jane Smith", result.get().getName());
        assertEquals("11", result.get().getStandard());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    @DisplayName("Should delete student by student ID")
    public void testDeleteStudentById_Success() {
        // Arrange
        String studentId = "2025-0001";
        doNothing().when(studentRepository).deleteByStudentId(studentId);

        // Act
        studentService.deleteStudentById(studentId);

        // Assert
        verify(studentRepository, times(1)).deleteByStudentId(studentId);
    }

    @Test
    @DisplayName("Should retrieve all students")
    public void testGetAllStudents_Success() {
        // Arrange
        List<Student> studentList = new ArrayList<>();
        studentList.add(student);

        Student student2 = Student.builder()
                .id(2L)
                .name("Jane Smith")
                .phoneNumber("9876543210")
                .alternateNumber("1234567890")
                .standard("11")
                .address("456 Oak St")
                .guardiansName("John Smith")
                .admissionDate(LocalDateTime.now())
                .studentId("2025-0002")
                .build();
        studentList.add(student2);

        StudentDTO studentDTO2 = new StudentDTO();
        studentDTO2.setName("Jane Smith");
        studentDTO2.setStudentId("2025-0002");

        when(studentRepository.findAll()).thenReturn(studentList);
        when(objectMapper.convertValue(student, StudentDTO.class))
                .thenReturn(studentDTO);
        when(objectMapper.convertValue(student2, StudentDTO.class))
                .thenReturn(studentDTO2);

        // Act
        List<StudentDTO> result = studentService.getAllStudents();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no students exist")
    public void testGetAllStudents_EmptyList() {
        // Arrange
        when(studentRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<StudentDTO> result = studentService.getAllStudents();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(studentRepository, times(1)).findAll();
    }
}
