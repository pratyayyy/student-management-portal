package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.dto.TransactionDTO;
import com.ija.student_management_portal.entity.UserCredential;
import com.ija.student_management_portal.repository.UserCredentialRepository;
import com.ija.student_management_portal.service.StudentService;
import com.ija.student_management_portal.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/student")
@Slf4j
public class StudentHomeController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @GetMapping("/home")
    public String studentHome(Authentication authentication, Model model) {
        return "forward:/index.html";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout=true";
    }
}
