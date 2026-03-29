package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.FeePayment;
import com.ija.student_management_portal.dto.TransactionDTO;
import com.ija.student_management_portal.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fees")
@Slf4j
public class ApiFeesController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<TransactionDTO>> getByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(transactionService.getTransactionById(studentId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody FeePayment feePayment) {
        try {
            TransactionDTO dto = TransactionDTO.builder()
                    .amount(feePayment.getAmount())
                    .transactionDate(LocalDateTime.now())
                    .paymentReceivedDate(feePayment.getPaymentReceivedDate())
                    .studentId(feePayment.getStudentId())
                    .paymentForMonth(feePayment.getPaymentForMonth())
                    .billNumber(feePayment.getBillNumber())
                    .build();
            transactionService.saveTransactionDetails(dto);
            return ResponseEntity.ok(Map.of("message", "Payment recorded"));
        } catch (Exception e) {
            log.error("Error recording fee payment", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to record payment"));
        }
    }
}
