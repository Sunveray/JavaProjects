package ru.ovchinnikov.CRM.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.ovchinnikov.CRM.model.Transaction;
import ru.ovchinnikov.CRM.model.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService_;

    @PostMapping
    public Transaction createTransaction(
            @RequestParam Integer sellerId,
            @RequestParam String paymentType,
            @RequestParam int amount) {

        return transactionService_.createTransaction(sellerId, paymentType, amount);
    }

    @GetMapping("/seller/{sellerId}")
    public List<Transaction> getSellerTransactions(@PathVariable Integer sellerId) {
        return transactionService_.getSellerTransactions(sellerId);
    }
}