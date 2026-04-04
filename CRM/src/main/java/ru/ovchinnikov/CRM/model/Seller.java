package ru.ovchinnikov.CRM.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_;
    private String name_;
    private String contactInfo_;
    private LocalDateTime registrationDate_;

    public void setName(String name_) {
        this.name_ = name_;
    }

    public void setContactInfo(String contactInfo_) {
        this.contactInfo_ = contactInfo_;
    }

    public void setRegistrationDate(LocalDateTime registrationDate_) {
        this.registrationDate_ = registrationDate_;
    }
}
