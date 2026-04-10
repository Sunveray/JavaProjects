package ru.ovchinnikov.CRM.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ovchinnikov.CRM.model.Transaction;
import java.util.List;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    @Query("SELECT t FROM Transaction t WHERE t.id_ = :id")
    Optional<Transaction> findCurrentById(@Param("id") Integer id);

    @Query("SELECT t FROM Transaction t WHERE t.seller_.id_ = :sellerId")
    List<Transaction> findAllTransactionsBySellerId(@Param("sellerId") Integer sellerId);
}