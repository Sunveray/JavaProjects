package ru.ovchinnikov.CRM.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_;
    public void setId(int id_) {
        this.id_ = id_;
    }
    public int getId() {
        return id_;
    }

    @ManyToOne
    private Seller seller_;
    public void setSeller(Seller seller_) {
        this.seller_ = seller_;
    }
    public Seller getSeller() {
        return seller_;
    }

    private int amount_;
    public void setAmount(int amount_) {
        this.amount_ = amount_;
    }
    public int getAmount() {
        return amount_;
    }

    private String paymentType_;
    public void setPaymentType(String paymentType_) {
        this.paymentType_ = paymentType_;
    }
    public String getPaymentType() {
        return paymentType_;
    }

    private LocalDateTime transcationDate_;
    public void setTranscationDate(LocalDateTime transcationDate_) {
        this.transcationDate_ = transcationDate_;
    }
    public LocalDateTime getTranscationDate() {
        return transcationDate_;
    }
}
