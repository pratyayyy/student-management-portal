package com.ija.student_management_portal.repository;

import com.ija.student_management_portal.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    List<Transaction> findByStudentId(String studentId);
}
