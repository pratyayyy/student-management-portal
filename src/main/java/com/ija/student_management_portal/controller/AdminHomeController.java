package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminHomeController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/home")
    public String adminHome(Authentication authentication, Model model) {
        String adminUsername = authentication.getName();
        log.info("Admin {} accessing admin home page", adminUsername);

        // Get all students for admin to manage
        List<StudentDTO> students = studentService.getAllStudents();
        model.addAttribute("students", students);
        model.addAttribute("adminUsername", adminUsername);
        model.addAttribute("totalStudents", students.size());

        return "admin-home";
    }

    @GetMapping("/students")
    public String manageStudents(Authentication authentication, Model model) {
        String adminUsername = authentication.getName();
        log.info("Admin {} viewing all students", adminUsername);

        List<StudentDTO> students = studentService.getAllStudents();
        model.addAttribute("students", students);
        model.addAttribute("adminUsername", adminUsername);

        return "admin-students";
    }
}
