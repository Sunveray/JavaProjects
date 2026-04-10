package ru.ovchinnikov.CRM.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ovchinnikov.CRM.model.Seller;
import java.util.List;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Integer> {
    List<Seller> findAll();

    @Query("SELECT t FROM Transaction t WHERE t.id_ = :id AND t.validTo_ IS NULL")
    Optional<Seller> findCurrentById(@Param("id") Integer id);

    @Query("SELECT t FROM Transaction t WHERE (t.originalId_ = :id OR t.id_ = :id) ORDER BY t.version_")
    List<Seller> findAllVersions(@Param("id") Integer id);
}