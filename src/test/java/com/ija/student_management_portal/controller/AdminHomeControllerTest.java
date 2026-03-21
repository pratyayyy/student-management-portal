package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminHomeController Tests")
public class AdminHomeControllerTest {

    @Mock
    private StudentService studentService;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @InjectMocks
    private AdminHomeController adminHomeController;

    private List<StudentDTO> studentList;

    @BeforeEach
    public void setUp() {
        studentList = new ArrayList<>();

        StudentDTO student1 = new StudentDTO();
        student1.setName("John Doe");
        student1.setStudentId("2025-0001");
        student1.setPhoneNumber("1234567890");
        student1.setStandard("10");
        studentList.add(student1);

        StudentDTO student2 = new StudentDTO();
        student2.setName("Jane Smith");
        student2.setStudentId("2025-0002");
        student2.setPhoneNumber("9876543210");
        student2.setStandard("11");
        studentList.add(student2);
    }

    @Test
    @DisplayName("Should return admin home page with students data")
    public void testAdminHome_Success() {
        // Arrange
        when(authentication.getName()).thenReturn("admin@test.com");
        when(studentService.getAllStudents()).thenReturn(studentList);

        // Act
        String result = adminHomeController.adminHome(authentication, model);

        // Assert
        assertEquals("admin-home", result);
        verify(studentService, times(1)).getAllStudents();
        verify(model, times(1)).addAttribute("students", studentList);
        verify(model, times(1)).addAttribute("adminUsername", "admin@test.com");
        verify(model, times(1)).addAttribute("totalStudents", 2);
    }

    @Test
    @DisplayName("Should return admin home page with empty students list")
    public void testAdminHome_EmptyStudentsList() {
        // Arrange
        when(authentication.getName()).thenReturn("admin@test.com");
        when(studentService.getAllStudents()).thenReturn(new ArrayList<>());

        // Act
        String result = adminHomeController.adminHome(authentication, model);

        // Assert
        assertEquals("admin-home", result);
        verify(studentService, times(1)).getAllStudents();
        verify(model, times(1)).addAttribute("students", new ArrayList<>());
        verify(model, times(1)).addAttribute("adminUsername", "admin@test.com");
        verify(model, times(1)).addAttribute("totalStudents", 0);
    }

    @Test
    @DisplayName("Should return admin students management page")
    public void testManageStudents_Success() {
        // Arrange
        when(authentication.getName()).thenReturn("admin@test.com");
        when(studentService.getAllStudents()).thenReturn(studentList);

        // Act
        String result = adminHomeController.manageStudents(authentication, model);

        // Assert
        assertEquals("admin-students", result);
        verify(studentService, times(1)).getAllStudents();
        verify(model, times(1)).addAttribute("students", studentList);
        verify(model, times(1)).addAttribute("adminUsername", "admin@test.com");
    }

    @Test
    @DisplayName("Should return admin students page with empty list")
    public void testManageStudents_EmptyList() {
        // Arrange
        when(authentication.getName()).thenReturn("admin@test.com");
        when(studentService.getAllStudents()).thenReturn(new ArrayList<>());

        // Act
        String result = adminHomeController.manageStudents(authentication, model);

        // Assert
        assertEquals("admin-students", result);
        verify(studentService, times(1)).getAllStudents();
        verify(model, times(1)).addAttribute("students", new ArrayList<>());
        verify(model, times(1)).addAttribute("adminUsername", "admin@test.com");
    }

    @Test
    @DisplayName("Should handle admin home with multiple students")
    public void testAdminHome_MultipleStudents() {
        // Arrange
        List<StudentDTO> largeStudentList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            StudentDTO student = new StudentDTO();
            student.setName("Student " + i);
            student.setStudentId("2025-" + String.format("%04d", i));
            largeStudentList.add(student);
        }

        when(authentication.getName()).thenReturn("admin@test.com");
        when(studentService.getAllStudents()).thenReturn(largeStudentList);

        // Act
        String result = adminHomeController.adminHome(authentication, model);

        // Assert
        assertEquals("admin-home", result);
        verify(model, times(1)).addAttribute("totalStudents", 10);
    }

    @Test
    @DisplayName("Should pass correct authentication username to model")
    public void testAdminHome_CorrectAuthenticationUsername() {
        // Arrange
        String adminUsername = "superadmin@institution.edu";
        when(authentication.getName()).thenReturn(adminUsername);
        when(studentService.getAllStudents()).thenReturn(studentList);

        // Act
        adminHomeController.adminHome(authentication, model);

        // Assert
        verify(model, times(1)).addAttribute("adminUsername", adminUsername);
    }
}
