//package com.ija.student_management_portal.controller;
//
//import com.ija.student_management_portal.dto.TransactionDTO;
//import com.ija.student_management_portal.service.TransactionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@RestController
//@RequestMapping(TransactionController.TRANSACTION_API_ENDPOINT)
//public class TransactionController {
//    public static final String TRANSACTION_API_ENDPOINT="/transactionmanagement";
//    public static final String SAVE_FEES_TRANSACTION_API_ENDPOINT="/savestudentfees";
//    public static final String DISPLAY_STUDENT_FEES_DETAILS_API_ENDPOINT ="/displaystudentfees/{id}";
//    public static final String UPDATE_STUDENT_FEES_DETAILS_API_ENDPOINT = "/updatefees/{id}";
//
//    @Autowired
//    private TransactionService transactionService;
//
//    @PostMapping(SAVE_FEES_TRANSACTION_API_ENDPOINT)
//    public ResponseEntity<?> saveTransaction(@RequestBody TransactionDTO transactionDTO){
//        Optional<TransactionDTO> saveTransaction = transactionService.saveTransactionDetails(transactionDTO);
//        if(saveTransaction.isPresent()){
//            return ResponseEntity.status(HttpStatus.CREATED).body(saveTransaction.get());
//        }
//        return ResponseEntity.status(HttpStatus.CONFLICT).body("Transaction error");
//    }
//
//
//    @GetMapping(DISPLAY_STUDENT_FEES_DETAILS_API_ENDPOINT)
//    public ResponseEntity<?> displayTransaction(@PathVariable Long id){
//        Optional<TransactionDTO> displayTransactionDTO = transactionService.getTransactionById(id);
//        if(displayTransactionDTO.isPresent()){
//            return ResponseEntity.status(HttpStatus.OK).body(displayTransactionDTO.get());
//        }
//        return ResponseEntity.status(HttpStatus.CONFLICT).body("Transaction data not found");
//    }
//
//
//    @PatchMapping(UPDATE_STUDENT_FEES_DETAILS_API_ENDPOINT)
//    public ResponseEntity<?> updateTransaction(@RequestBody TransactionDTO transactionDTO, @PathVariable Long id){
//
//        Optional<TransactionDTO> updateTransactionDTO = transactionService.updateTransaction(id, transactionDTO);
//        if (updateTransactionDTO.isPresent()) {
//            return ResponseEntity.status(HttpStatus.OK).body(updateTransactionDTO.get());
//        }
//        return ResponseEntity.status(HttpStatus.CONFLICT).body("Transaction data not found.");
//
//    }
//
//}
