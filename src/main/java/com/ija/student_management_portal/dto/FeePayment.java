package com.ija.student_management_portal.dto;

import java.time.LocalDate;

public class FeePayment {
    private String studentId;
    private int amount;
    private LocalDate paymentReceivedDate;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentReceivedDate() {
        return paymentReceivedDate;
    }

    public void setPaymentReceivedDate(LocalDate paymentReceivedDate) {
        this.paymentReceivedDate = paymentReceivedDate;
    }

}

