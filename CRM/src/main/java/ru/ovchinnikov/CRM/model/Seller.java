package ru.ovchinnikov.CRM.model;

import jakarta.persistence.*;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "Sellers")
public class Seller {
    public Seller(){};

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_;
    public void setId(int id_) {
        this.id_ = id_;
    }
    public int getId() {
        return id_;
    }

    @Column(nullable = false, length = 100)
    private String name_;
    public String getName() {
        return name_;
    }
    public void setName(String name_) {
        this.name_ = name_;
    }

    @Column(nullable = false, length = 100)
    private String contactInfo_;
    public void setContactInfo(String contactInfo_) {
        this.contactInfo_ = contactInfo_;
    }
    public String getContactInfo() {
        return contactInfo_;
    }

    @Column(nullable = false, length = 100)
    private LocalDateTime registrationDate_;
    public void setRegistrationDate(LocalDateTime registrationDate_) {
        this.registrationDate_ = registrationDate_;
    }
    public LocalDateTime getRegistrationDate() {
        return registrationDate_;
    }

    @OneToMany(mappedBy = "seller_")
    private List<Transaction> transactions_;

    @Column(name = "valid_from")
    private LocalDateTime validFrom_;
    public LocalDateTime getValidFrom() {
        return validFrom_;
    }
    public void setValidFrom(LocalDateTime validFrom_) {
        this.validFrom_ = validFrom_;
    }

    @Column(name = "valid_to")
    private LocalDateTime validTo_;
    public LocalDateTime getValidTo() {
        return validTo_;
    }
    public void setValidTo(LocalDateTime validTo_) {
        this.validTo_ = validTo_;
    }

    @Column(name = "is_current")
    private Boolean isCurrent_ = true;
    public Boolean getCurrent() {
        return isCurrent_;
    }
    public void setCurrent(Boolean current_) {
        isCurrent_ = current_;
    }

    @Column(name = "original_id")
    private Integer originalId_;
    public Integer getOriginalId() {
        return originalId_;
    }
    public void setOriginalId(Integer originalId) {
        if (originalId_!= null){
            originalId_ = originalId;
        }
        else{
            originalId_ = getId();
        }
    }

    @Column(name = "version")
    private Integer version_ = 1;
    public void setVersion(Integer version_) {
        this.version_ = version_;
    }
    public Integer getVersion() {
        return version_;
    }
}

