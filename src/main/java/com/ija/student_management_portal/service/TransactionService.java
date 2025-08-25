package com.ija.student_management_portal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.dto.TransactionDTO;
import com.ija.student_management_portal.entity.Student;
import com.ija.student_management_portal.entity.Transaction;
import com.ija.student_management_portal.repository.StudentRepository;
import com.ija.student_management_portal.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
    }

    @Autowired
    private ObjectMapper objectmapper;

    public Optional<TransactionDTO> saveTransactionDetails(TransactionDTO transactionDTO){
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setTransactionDate(transactionDTO.getTransactionDate());
        transaction.setStudentId(transactionDTO.getStudentId());
        transaction.setMonth(transactionDTO.getMonth());

        Transaction transactionEntity = transactionRepository.save(transaction);
        TransactionDTO trnscDTO = objectmapper.convertValue(transactionEntity,TransactionDTO.class);
        return Optional.of(trnscDTO);
    }

    public List<TransactionDTO> getTransactionById(String id){
        List<Transaction> transactionEntity = transactionRepository.findByStudentId(id);
        List<TransactionDTO> transactions = transactionEntity.stream()
                .map(transaction -> objectmapper.convertValue(transaction, TransactionDTO.class))
                .collect(Collectors.toList());
        return transactions;
    }

    public Optional<TransactionDTO> updateTransaction(Long id,TransactionDTO transactionDTO){
        Optional<Transaction> existingTransactionEntity = transactionRepository.findById(id);
        Transaction existingTransaction = existingTransactionEntity.orElseGet(null);
        existingTransaction.setAmount(transactionDTO.getAmount());
        existingTransaction.setTransactionDate(transactionDTO.getTransactionDate());

        Transaction updatedTransaction = transactionRepository.save(existingTransaction);
        TransactionDTO updatedTrnscDTO = objectmapper.convertValue(updatedTransaction,TransactionDTO.class);
        return Optional.of(updatedTrnscDTO);
    }
}
