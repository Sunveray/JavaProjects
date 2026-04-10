package ru.ovchinnikov.CRM.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ovchinnikov.CRM.model.Seller;
import java.util.List;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Integer> {
    @Query("SELECT s FROM Seller s WHERE s.originalId_ = :id ORDER BY s.version_ ASC")
    List<Seller> findAllVersions(@Param("id") Integer id);

    @Query("SELECT s FROM Seller s WHERE s.originalId_ = :id AND s.isCurrent_ = true")
    Optional<Seller> findCurrentById(@Param("id") Integer id);

    @Query("SELECT t.seller_ FROM Transaction t " +
       "GROUP BY t.seller_ " +
       "ORDER BY SUM(t.amount_) DESC " +
       "LIMIT 1")
    Optional<Seller> findTopSellerByTotalAmount();

    @Query("SELECT t.seller_ FROM Transaction t " +
           "GROUP BY t.seller_ " +
           "HAVING SUM(t.amount_) < :amount")
    List<Seller> findSellersTotalAmountLessThan(@Param("amount") Integer amount);

}