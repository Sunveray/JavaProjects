package ru.ovchinnikov.CRM.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ovchinnikov.CRM.repositories.SellerRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SellerService {

    public SellerService(){}

    @Autowired
    private SellerRepository sellerRepository;

    @Transactional
    public Seller updateSeller(Integer id, String newName){
        Seller current = sellerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Seller not found with id: " + id));

        LocalDateTime now = LocalDateTime.now();
        current.setValidTo(now);
        current.setCurrent(false);
        sellerRepository.save(current);

        Seller newVersion = new Seller();
        newVersion.setName(newName);
        newVersion.setContactInfo(current.getContactInfo());
        newVersion.setRegistrationDate(current.getRegistrationDate());

        newVersion.setValidFrom(now);
        newVersion.setValidTo(null);
        newVersion.setCurrent(true);
        newVersion.setOriginalId(id);

        return sellerRepository.save(newVersion);
    }

    public List<Seller> getSellerHistory(Integer id) {
        Seller current = sellerRepository.findCurrentById(id)
            .orElseThrow(() -> new RuntimeException("Seller not found with id: " + id));
        return sellerRepository.findAllVersions(id);
    }

    @Transactional
    public Seller createSeller(String name, String contactInfo) {
        Seller seller = new Seller();
        seller.setName(name);
        seller.setContactInfo(contactInfo);
        seller.setCurrent(true);
        seller.setValidFrom(LocalDateTime.now());
        seller.setRegistrationDate(LocalDateTime.now());
        seller.setVersion(1);

        Seller saved = sellerRepository.save(seller);
        saved.setOriginalId(saved.getId());
        return sellerRepository.save(saved);
    }

    @Transactional
    public void deleteSeller(Integer id){
        sellerRepository.deleteById(id);
    }

    public Seller getCurrentSeller(Integer id) {
        return sellerRepository.findCurrentById(id)
            .orElseThrow(() -> new RuntimeException("Seller not found"));
    }

    public List<Seller> getAllSellers() {
        return sellerRepository.findAll();
    }

    @Transactional
    public Seller getBestSeller() {
        Optional<Seller> result = sellerRepository.findTopSellerByTotalAmount();

        if (result.isPresent()) {
            return result.get();
        } else {
            throw new RuntimeException("No sellers with transactions");
        }
    }

   public List<Seller> getSellersWithLowRevenue(Integer maxAmount) {
        return sellerRepository.findSellersTotalAmountLessThan(maxAmount);}
}
