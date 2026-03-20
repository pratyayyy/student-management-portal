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
        String username = authentication.getName();
        log.info("Student {} accessing home page", username);

        // Get student ID from user credentials
        Optional<UserCredential> userCredential = userCredentialRepository.findByUsername(username);
        if (userCredential.isPresent()) {
            String studentId = userCredential.get().getStudentId();
            Optional<StudentDTO> student = studentService.getStudentById(studentId);

            if (student.isPresent()) {
                model.addAttribute("student", student.get());
                model.addAttribute("username", username);

                // Get transactions for this student
                List<TransactionDTO> transactions = transactionService.getTransactionById(studentId);
                model.addAttribute("transactions", transactions);
                model.addAttribute("studentId", studentId);

                return "student-home";
            }
        }

        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout=true";
    }
}
