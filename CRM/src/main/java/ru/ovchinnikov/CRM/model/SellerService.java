package ru.ovchinnikov.CRM.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ovchinnikov.CRM.repositories.SellerRepository;
import java.time.LocalDateTime;
import java.util.List;

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

    @Transactional
    public Seller createSeller(String name, String contactInfo){
        Seller seller = new Seller();

        seller.setValidTo(null);
        seller.setName(name);
        seller.setId(seller.getId());
        seller.setContactInfo(contactInfo);
        seller.setCurrent(true);
        seller.setValidFrom(LocalDateTime.now());
        seller.setRegistrationDate(LocalDateTime.now());
        seller.setOriginalId(seller.getId());
        seller.setVersion(1);

        return sellerRepository.save(seller);
    }

    @Transactional
    public void deleteSeller(Integer id){
        sellerRepository.deleteById(id);
    }

    public Seller getCurrentSeller(Integer id) {
        return sellerRepository.findCurrentById(id)
            .orElseThrow(() -> new RuntimeException("Seller not found"));
    }

    public List<Seller> getSellerHistory(Integer id) {
        return sellerRepository.findAllVersions(id);
    }

    @Transactional
    public void getBestSeller() {
        List<Seller> sellerList = sellerRepository.findAll();
        for (int i = 0; i<sellerList.size(); i++){
            System.out.println(sellerList.get(i));
        }
    }
}
