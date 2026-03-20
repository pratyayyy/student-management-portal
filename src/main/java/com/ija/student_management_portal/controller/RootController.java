package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.FeePayment;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.dto.TransactionDTO;
import com.ija.student_management_portal.entity.Student;
import com.ija.student_management_portal.service.StudentService;
import com.ija.student_management_portal.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
public class RootController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/home")
    public String homePage(Model model) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Check if user has ADMIN role
        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        log.info("Home page accessed by user: {} (Admin: {})", username, isAdmin);

        List<StudentDTO> students = studentService.getAllStudents();
        model.addAttribute("students", students);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("username", username);

        return "home";
    }

    @GetMapping("/students/{id}")
    public String studentDetails(@PathVariable String id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<StudentDTO> student = studentService.getStudentById(id);
        List<TransactionDTO> transactions = transactionService.getTransactionById(id);

        model.addAttribute("student", student.get());
        model.addAttribute("transactions", transactions);
        model.addAttribute("username", username);

        return "student-details";
    }

    @GetMapping("/accept/{studentId}")
    public String showAcceptFeeForm(@PathVariable String studentId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        FeePayment feePayment = new FeePayment();
        feePayment.setStudentId(studentId);
        model.addAttribute("feePayment", feePayment);
        model.addAttribute("username", username);
        return "accept_fee";
    }

    @PostMapping("/fees/accept")
    public String acceptFee(@ModelAttribute FeePayment feePayment) {

        TransactionDTO transactionDTO = TransactionDTO.builder()
                        .amount(feePayment.getAmount())
                        .transactionDate(LocalDateTime.now())
                        .month(feePayment.getFeeMonth())
                        .studentId(feePayment.getStudentId())
                        .build();

        transactionService.saveTransactionDetails(transactionDTO);
        return "redirect:/students/" + feePayment.getStudentId();
    }

    @GetMapping("/add")
    public String showAddStudentForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("student", new StudentDTO());
        model.addAttribute("username", username);
        return "add_student";
    }

    @PostMapping("/students/add")
    public String registerStudent(@ModelAttribute StudentDTO studentDTO) {
        studentService.saveStudent(studentDTO);
        return "redirect:/home"; // Redirect to home page or student list
    }
}
