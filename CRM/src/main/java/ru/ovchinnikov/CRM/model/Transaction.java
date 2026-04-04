package ru.ovchinnikov.CRM.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_;
    private Seller seller_;
    private int amount_;
    private String paymentType_;
    private LocalDateTime transcationDate_;

    public void setAmount(int amount_) {
        this.amount_ = amount_;
    }

    public void setSeller(Seller seller_) {
        this.seller_ = seller_;
    }

    public void setTranscationDate(LocalDateTime transcationDate_) {
        this.transcationDate_ = transcationDate_;
    }

    public void setPaymentType(String paymentType_) {
        this.paymentType_ = paymentType_;
    }
}
