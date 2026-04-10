package ru.ovchinnikov.CRM.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ovchinnikov.CRM.model.Transaction;
import java.util.List;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    @Query("SELECT s FROM Seller s WHERE s.id_ = :id AND s.validTo_ IS NULL")
    Optional<Transaction> findCurrentById(@Param("id") Integer id);

    @Query("SELECT s FROM Seller s WHERE (s.originalId_ = :id OR s.id_ = :id) ORDER BY s.version_")
    List<Transaction> findAllTransactions(@Param("id") Integer id);
}