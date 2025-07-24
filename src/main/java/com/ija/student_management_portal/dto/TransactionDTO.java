package com.ija.student_management_portal.dto;

import java.time.LocalDateTime;

public class TransactionDTO {
    private Long id;
    private LocalDateTime date;
    private int amount;

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public TransactionDTO(LocalDateTime date, Long id, int amount) {
        this.date = date;
        this.id = id;
        this.amount = amount;
    }

    public TransactionDTO() {

    }
}
