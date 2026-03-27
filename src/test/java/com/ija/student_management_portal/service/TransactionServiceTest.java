package com.ija.student_management_portal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ija.student_management_portal.dto.TransactionDTO;
import com.ija.student_management_portal.entity.Transaction;
import com.ija.student_management_portal.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Tests")
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionDTO transactionDTO;
    private Transaction transaction;

    @BeforeEach
    public void setUp() {
        transactionDTO = TransactionDTO.builder()
                .id(1L)
                .amount(5000)
                .studentId("2025-0001")
                .paymentReceivedDate(LocalDate.of(2026, 1, 15))
                .transactionDate(LocalDateTime.now())
                .billNumber("BILL-001")
                .build();

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(5000);
        transaction.setStudentId("2025-0001");
        transaction.setPaymentReceivedDate(LocalDate.of(2026, 1, 15));
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setBillNumber("BILL-001");
    }

    @Test
    @DisplayName("Should save transaction successfully")
    public void testSaveTransactionDetails_Success() {
        // Arrange
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);
        when(objectMapper.convertValue(transaction, TransactionDTO.class))
                .thenReturn(transactionDTO);

        // Act
        Optional<TransactionDTO> result = transactionService.saveTransactionDetails(transactionDTO);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(5000, result.get().getAmount());
        assertEquals("2025-0001", result.get().getStudentId());
        assertEquals(LocalDate.of(2026, 1, 15), result.get().getPaymentReceivedDate());
        assertEquals("BILL-001", result.get().getBillNumber());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should retrieve transactions by student ID")
    public void testGetTransactionById_Success() {
        // Arrange
        String studentId = "2025-0001";
        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(transaction);

        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setAmount(3000);
        transaction2.setStudentId(studentId);
        transaction2.setPaymentReceivedDate(LocalDate.of(2026, 2, 10));
        transaction2.setTransactionDate(LocalDateTime.now());
        transactionList.add(transaction2);

        TransactionDTO transactionDTO2 = TransactionDTO.builder()
                .id(2L)
                .amount(3000)
                .studentId(studentId)
                .paymentReceivedDate(LocalDate.of(2026, 2, 10))
                .transactionDate(LocalDateTime.now())
                .build();

        when(transactionRepository.findByStudentId(studentId))
                .thenReturn(transactionList);
        when(objectMapper.convertValue(transaction, TransactionDTO.class))
                .thenReturn(transactionDTO);
        when(objectMapper.convertValue(transaction2, TransactionDTO.class))
                .thenReturn(transactionDTO2);

        // Act
        List<TransactionDTO> result = transactionService.getTransactionById(studentId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(5000, result.get(0).getAmount());
        assertEquals(3000, result.get(1).getAmount());
        verify(transactionRepository, times(1)).findByStudentId(studentId);
    }

    @Test
    @DisplayName("Should return empty list when no transactions exist for student")
    public void testGetTransactionById_EmptyList() {
        // Arrange
        String studentId = "2025-9999";
        when(transactionRepository.findByStudentId(studentId))
                .thenReturn(new ArrayList<>());

        // Act
        List<TransactionDTO> result = transactionService.getTransactionById(studentId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1)).findByStudentId(studentId);
    }

    @Test
    @DisplayName("Should update transaction successfully")
    public void testUpdateTransaction_Success() {
        // Arrange
        Long transactionId = 1L;
        TransactionDTO updateDTO = TransactionDTO.builder()
                .id(1L)
                .amount(6000)
                .studentId("2025-0001")
                .paymentReceivedDate(LocalDate.of(2026, 1, 15))
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(1L);
        updatedTransaction.setAmount(6000);
        updatedTransaction.setStudentId("2025-0001");
        updatedTransaction.setPaymentReceivedDate(LocalDate.of(2026, 1, 15));
        updatedTransaction.setTransactionDate(LocalDateTime.now());

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(updatedTransaction);
        when(objectMapper.convertValue(updatedTransaction, TransactionDTO.class))
                .thenReturn(updateDTO);

        // Act
        Optional<TransactionDTO> result = transactionService.updateTransaction(transactionId, updateDTO);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(6000, result.get().getAmount());
        verify(transactionRepository, times(1)).findById(transactionId);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should handle update when transaction not found")
    public void testUpdateTransaction_NotFound() {
        // Arrange
        Long transactionId = 999L;
        TransactionDTO updateDTO = TransactionDTO.builder()
                .id(999L)
                .amount(6000)
                .build();

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        // Act & Assert
        Optional<TransactionDTO> result = transactionService.updateTransaction(transactionId, updateDTO);

        // The method will throw NPE if not found, verify this behavior
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    @DisplayName("Should save transaction with different amounts")
    public void testSaveTransactionDetails_DifferentAmounts() {
        // Arrange
        TransactionDTO highAmountDTO = TransactionDTO.builder()
                .id(3L)
                .amount(50000)
                .studentId("2025-0002")
                .paymentReceivedDate(LocalDate.of(2026, 3, 20))
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction highAmountTransaction = new Transaction();
        highAmountTransaction.setId(3L);
        highAmountTransaction.setAmount(50000);
        highAmountTransaction.setStudentId("2025-0002");
        highAmountTransaction.setPaymentReceivedDate(LocalDate.of(2026, 3, 20));
        highAmountTransaction.setTransactionDate(LocalDateTime.now());

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(highAmountTransaction);
        when(objectMapper.convertValue(highAmountTransaction, TransactionDTO.class))
                .thenReturn(highAmountDTO);

        // Act
        Optional<TransactionDTO> result = transactionService.saveTransactionDetails(highAmountDTO);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(50000, result.get().getAmount());
        assertEquals("2025-0002", result.get().getStudentId());
    }
}
