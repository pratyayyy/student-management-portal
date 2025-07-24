package com.ija.student_management_portal.repository;

import com.ija.student_management_portal.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
}
