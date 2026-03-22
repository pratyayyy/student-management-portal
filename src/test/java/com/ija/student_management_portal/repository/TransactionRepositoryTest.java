package com.ija.student_management_portal.repository;

import com.ija.student_management_portal.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("TransactionRepository Tests")
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Transaction transaction;

    @BeforeEach
    public void setUp() {
        transaction = new Transaction();
        transaction.setAmount(5000);
        transaction.setStudentId("2025-0001");
        transaction.setPaymentReceivedDate(LocalDate.of(2026, 1, 15));
        transaction.setTransactionDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should save transaction successfully")
    public void testSaveTransaction_Success() {
        // Act
        Transaction savedTransaction = transactionRepository.save(transaction);
        testEntityManager.flush();

        // Assert
        assertNotNull(savedTransaction.getId());
        assertEquals(5000, savedTransaction.getAmount());
        assertEquals("2025-0001", savedTransaction.getStudentId());
    }

    @Test
    @DisplayName("Should find transactions by student ID")
    public void testFindByStudentId_Success() {
        // Arrange
        testEntityManager.persistAndFlush(transaction);

        Transaction transaction2 = new Transaction();
        transaction2.setAmount(3000);
        transaction2.setStudentId("2025-0001");
        transaction2.setPaymentReceivedDate(LocalDate.of(2026, 2, 10));
        transaction2.setTransactionDate(LocalDateTime.now());
        testEntityManager.persistAndFlush(transaction2);

        // Act
        List<Transaction> transactions = transactionRepository.findByStudentId("2025-0001");

        // Assert
        assertNotNull(transactions);
        assertEquals(2, transactions.size());
        assertTrue(transactions.stream().anyMatch(t -> t.getAmount() == 5000));
        assertTrue(transactions.stream().anyMatch(t -> t.getAmount() == 3000));
    }

    @Test
    @DisplayName("Should return empty list when no transactions for student")
    public void testFindByStudentId_Empty() {
        // Act
        List<Transaction> transactions = transactionRepository.findByStudentId("2025-9999");

        // Assert
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }

    @Test
    @DisplayName("Should find transaction by ID")
    public void testFindById_Success() {
        // Arrange
        Transaction savedTransaction = testEntityManager.persistAndFlush(transaction);

        // Act
        Optional<Transaction> foundTransaction = transactionRepository.findById(savedTransaction.getId());

        // Assert
        assertTrue(foundTransaction.isPresent());
        assertEquals(5000, foundTransaction.get().getAmount());
        assertEquals(LocalDate.of(2026, 1, 15), foundTransaction.get().getPaymentReceivedDate());
    }

    @Test
    @DisplayName("Should update transaction successfully")
    public void testUpdateTransaction_Success() {
        // Arrange
        Transaction savedTransaction = testEntityManager.persistAndFlush(transaction);

        // Act
        savedTransaction.setAmount(6000);
        Transaction updatedTransaction = transactionRepository.save(savedTransaction);
        testEntityManager.flush();

        // Assert
        assertEquals(6000, updatedTransaction.getAmount());
    }

    @Test
    @DisplayName("Should delete transaction successfully")
    public void testDeleteTransaction_Success() {
        // Arrange
        Transaction savedTransaction = testEntityManager.persistAndFlush(transaction);
        Long transactionId = savedTransaction.getId();

        // Act
        transactionRepository.deleteById(transactionId);
        testEntityManager.flush();

        // Assert
        Optional<Transaction> deletedTransaction = transactionRepository.findById(transactionId);
        assertTrue(deletedTransaction.isEmpty());
    }

    @Test
    @DisplayName("Should find all transactions")
    public void testFindAll_Success() {
        // Arrange
        testEntityManager.persistAndFlush(transaction);

        Transaction transaction2 = new Transaction();
        transaction2.setAmount(2000);
        transaction2.setStudentId("2025-0002");
        transaction2.setPaymentReceivedDate(LocalDate.of(2026, 2, 10));
        transaction2.setTransactionDate(LocalDateTime.now());
        testEntityManager.persistAndFlush(transaction2);

        // Act
        List<Transaction> transactions = transactionRepository.findAll();

        // Assert
        assertNotNull(transactions);
        assertTrue(transactions.size() >= 2);
    }

    @Test
    @DisplayName("Should handle multiple transactions for same student different dates")
    public void testMultipleTransactionsSameStudent() {
        // Arrange
        Transaction trans1 = new Transaction();
        trans1.setAmount(5000);
        trans1.setStudentId("2025-0001");
        trans1.setPaymentReceivedDate(LocalDate.of(2026, 1, 15));
        trans1.setTransactionDate(LocalDateTime.now());
        testEntityManager.persistAndFlush(trans1);

        Transaction trans2 = new Transaction();
        trans2.setAmount(5000);
        trans2.setStudentId("2025-0001");
        trans2.setPaymentReceivedDate(LocalDate.of(2026, 2, 10));
        trans2.setTransactionDate(LocalDateTime.now());
        testEntityManager.persistAndFlush(trans2);

        Transaction trans3 = new Transaction();
        trans3.setAmount(5000);
        trans3.setStudentId("2025-0001");
        trans3.setPaymentReceivedDate(LocalDate.of(2026, 3, 20));
        trans3.setTransactionDate(LocalDateTime.now());
        testEntityManager.persistAndFlush(trans3);

        // Act
        List<Transaction> transactions = transactionRepository.findByStudentId("2025-0001");

        // Assert
        assertEquals(3, transactions.size());
    }
}
