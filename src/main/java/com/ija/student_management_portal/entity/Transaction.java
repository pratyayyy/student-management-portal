package com.ija.student_management_portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "transaction_details")
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private LocalDateTime transactionDate;
    private int amount;
    private String studentId;
    private LocalDate paymentReceivedDate;

    private String paymentForMonth;

}
