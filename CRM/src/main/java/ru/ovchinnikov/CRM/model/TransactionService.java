package ru.ovchinnikov.CRM.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import ru.ovchinnikov.CRM.repositories.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionService {
    public TransactionService(){}

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction createTransaction(Seller seller, Integer id,
                                         String paymentType, int amount){
        Transaction transaction = new Transaction();

        transaction.setSeller(seller);
        transaction.setId(id);
        transaction.setTranscationDate(LocalDateTime.now());
        transaction.setPaymentType(paymentType);
        transaction.setAmount(amount);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public List<Transaction> getSellerTransactions(Integer id){
        return transactionRepository.findAllTransactions(id);
    }
}
