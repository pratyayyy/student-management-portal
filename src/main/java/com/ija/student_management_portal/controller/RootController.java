package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.FeePayment;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.dto.TransactionDTO;
import com.ija.student_management_portal.entity.Student;
import com.ija.student_management_portal.service.StudentService;
import com.ija.student_management_portal.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RootController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/")
    public String homePage(Model model) {
        List<StudentDTO> students = studentService.getAllStudents();
        model.addAttribute("students", students);
        return "home";
    }

    @GetMapping("/students/{id}")
    public String studentDetails(@PathVariable String id, Model model) {
        Optional<StudentDTO> student = studentService.getStudentById(id);
        List<TransactionDTO> transactions = transactionService.getTransactionById(id);

        model.addAttribute("student", student.get());
        model.addAttribute("transactions", transactions);

        return "student-details";
    }

    @GetMapping("/accept/{studentId}")
    public String showAcceptFeeForm(@PathVariable String studentId, Model model) {
        FeePayment feePayment = new FeePayment();
        feePayment.setStudentId(studentId);
        model.addAttribute("feePayment", feePayment);
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
        model.addAttribute("student", new StudentDTO());
        return "add_student";
    }

    @PostMapping("/students/add")
    public String registerStudent(@ModelAttribute StudentDTO studentDTO) {
        studentService.saveStudent(studentDTO);
        return "redirect:/"; // Redirect to home page or student list
    }
}
