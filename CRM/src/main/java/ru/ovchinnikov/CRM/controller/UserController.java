package ru.ovchinnikov.CRM.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ovchinnikov.CRM.model.Seller;
import ru.ovchinnikov.CRM.repositories.SellerRepository;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sellers")
public class UserController {
    @Autowired
    private SellerRepository sellerRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSeller(@PathVariable Integer id){
        try{
            Optional<Seller> sellerOptional = sellerRepository.findById(id);

            if (sellerOptional.isPresent()){
                return ResponseEntity.ok(sellerOptional.get());
            }
            else{
                return ResponseEntity.notFound().build();
            }
        }
        catch (Exception e){
            e.getStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public List<Seller> getAllSellers(){
        return sellerRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Seller> createSeller(@RequestBody Seller seller){
        seller.setRegistrationDate(LocalDateTime.now());
        Seller savedSeller = sellerRepository.save(seller);
        return ResponseEntity.created(URI.create("/api/sellers/" + savedSeller.getId())).body(savedSeller);
    }
}
