package ru.ovchinnikov.CRM.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ovchinnikov.CRM.repositories.SellerRepository;
import ru.ovchinnikov.CRM.repositories.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    public TransactionService(){}

    @Autowired
    private TransactionRepository transactionRepository_;

    @Autowired
    private SellerRepository sellerRepository_;

    @Transactional
    public Transaction createTransaction(Integer sellerId, String paymentType, int amount) {
        Seller seller = sellerRepository_.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("Seller not found with id: " + sellerId));

        Transaction transaction = new Transaction();
        transaction.setSeller(seller);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setPaymentType(paymentType);
        transaction.setAmount(amount);

        return transactionRepository_.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getSellerTransactions(Integer sellerId) {
        if (!sellerRepository_.existsById(sellerId)) {
            throw new RuntimeException("Seller not found with id: " + sellerId);
        }
        return transactionRepository_.findAllTransactionsBySellerId(sellerId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository_.findAll();
    }

    @Transactional(readOnly = true)
    public Transaction getTransactionById(Integer id) {
        return transactionRepository_.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }
}
