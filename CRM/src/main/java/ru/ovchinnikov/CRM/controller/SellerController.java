package ru.ovchinnikov.CRM.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ovchinnikov.CRM.model.Seller;
import ru.ovchinnikov.CRM.model.SellerService;
import ru.ovchinnikov.CRM.repositories.SellerRepository;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sellers")
public class SellerController {
    @Autowired
    private final SellerService sellerService_;

    public SellerController(SellerService sellerService) {
        sellerService_ = sellerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSeller(@PathVariable Integer id) {
        try {
            Seller seller = sellerService_.getCurrentSeller(id);
            return ResponseEntity.ok(seller);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public List<Seller> getAllSellers() {
        return sellerService_.getAllSellers();
    }

    @GetMapping("/best")
    public ResponseEntity<Seller> getBestSeller() {
        try {
            Seller best = sellerService_.getBestSeller();
            return ResponseEntity.ok(best);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/low-revenue")
    public ResponseEntity<List<Seller>> getLowRevenueSellers(
            @RequestParam Integer maxAmount) {
        List<Seller> sellers = sellerService_.getSellersWithLowRevenue(maxAmount);
        return ResponseEntity.ok(sellers);
    }

    @PostMapping
    public ResponseEntity<Seller> createSeller(@RequestBody Seller seller) {
        seller.setRegistrationDate(LocalDateTime.now());
        Seller savedSeller = sellerService_.createSeller(seller.getName(), seller.getContactInfo());
        return ResponseEntity.created(URI.create("/sellers/" + savedSeller.getId()))
                             .body(savedSeller);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<Seller>> getSellerHistory(@PathVariable Integer id) {
        try {
            List<Seller> history = sellerService_.getSellerHistory(id);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
}
}
