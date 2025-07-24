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

import java.util.Optional;

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
        transaction.setDate(transactionDTO.getDate());

        Transaction transactionEntity = transactionRepository.save(transaction);
        TransactionDTO trnscDTO = objectmapper.convertValue(transactionEntity,TransactionDTO.class);
        return Optional.of(trnscDTO);
    }

    public Optional<TransactionDTO> getTransactionById(Long id){
        Optional<Transaction> transactionEntity = transactionRepository.findById(id);
        TransactionDTO trnscDTO = objectmapper.convertValue(transactionEntity,TransactionDTO.class);
        return Optional.of(trnscDTO);
    }

    public Optional<TransactionDTO> updateTransaction(Long id,TransactionDTO transactionDTO){
        Optional<Transaction> existingTransactionEntity = transactionRepository.findById(id);
        Transaction existingTransaction = existingTransactionEntity.orElseGet(null);
        existingTransaction.setAmount(transactionDTO.getAmount());
        existingTransaction.setDate(transactionDTO.getDate());

        Transaction updatedTransaction = transactionRepository.save(existingTransaction);
        TransactionDTO updatedTrnscDTO = objectmapper.convertValue(updatedTransaction,TransactionDTO.class);
        return Optional.of(updatedTrnscDTO);
    }
}
