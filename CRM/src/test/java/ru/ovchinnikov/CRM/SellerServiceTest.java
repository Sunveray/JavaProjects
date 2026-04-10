package ru.ovchinnikov.CRM;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.ovchinnikov.CRM.model.Seller;
import ru.ovchinnikov.CRM.model.SellerService;
import ru.ovchinnikov.CRM.model.TransactionService;
import ru.ovchinnikov.CRM.repositories.SellerRepository;
import ru.ovchinnikov.CRM.repositories.TransactionRepository;
import ru.ovchinnikov.CRM.ui.CrmApplication;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = CrmApplication.class)
@ActiveProfiles("test")
@Transactional
class SellerServiceTest {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Seller testSeller;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        sellerRepository.deleteAll();
        testSeller = sellerService.createSeller("Ivan Petrov", "ivan@mail.ru");
    }

    @Test
    void createSeller_ShouldSaveAndReturnSeller() {
        Seller newSeller = sellerService.createSeller("Maria Sidorova", "maria@mail.ru");

        assertThat(newSeller).isNotNull();
        assertThat(newSeller.getId()).isNotNull();
        assertThat(newSeller.getId()).isGreaterThan(0);
        assertThat(newSeller.getName()).isEqualTo("Maria Sidorova");
        assertThat(newSeller.getContactInfo()).isEqualTo("maria@mail.ru");
        assertThat(newSeller.getCurrent()).isTrue();
        assertThat(newSeller.getVersion()).isEqualTo(1);
        assertThat(newSeller.getValidFrom()).isNotNull();
        assertThat(newSeller.getRegistrationDate()).isNotNull();
        assertThat(newSeller.getValidTo()).isNull();

        Seller saved = sellerRepository.findById(newSeller.getId()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Maria Sidorova");
    }

    @Test
    void createSeller_ShouldSetOriginalIdEqualToId() {
        Seller newSeller = sellerService.createSeller("Petr Ivanov", "petr@mail.ru");
        assertThat(newSeller.getOriginalId()).isEqualTo(newSeller.getId());
    }

    @Test
    void createSeller_ShouldSetValidFromAndRegistrationDate() {
        Seller newSeller = sellerService.createSeller("Anna Smirnova", "anna@mail.ru");
        assertThat(newSeller.getValidFrom()).isNotNull();
        assertThat(newSeller.getRegistrationDate()).isNotNull();
        assertThat(newSeller.getValidTo()).isNull();
    }

    @Test
    void createSeller_MultipleSellers_ShouldHaveUniqueIds() {
        Seller seller1 = sellerService.createSeller("First", "first@mail.ru");
        Seller seller2 = sellerService.createSeller("Second", "second@mail.ru");
        Seller seller3 = sellerService.createSeller("Third", "third@mail.ru");

        assertThat(seller1.getId()).isNotEqualTo(seller2.getId());
        assertThat(seller1.getId()).isNotEqualTo(seller3.getId());
        assertThat(seller2.getId()).isNotEqualTo(seller3.getId());
        assertThat(sellerRepository.count()).isEqualTo(4);
    }

    @Test
    void getCurrentSeller_WhenExists_ShouldReturnSeller() {
        Seller found = sellerService.getCurrentSeller(testSeller.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(testSeller.getId());
        assertThat(found.getName()).isEqualTo("Ivan Petrov");
        assertThat(found.getContactInfo()).isEqualTo("ivan@mail.ru");
        assertThat(found.getCurrent()).isTrue();
    }

    @Test
    void getCurrentSeller_WhenNotExists_ShouldThrowException() {
        assertThatThrownBy(() -> sellerService.getCurrentSeller(99999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Seller not found");
    }

    @Test
    void getCurrentSeller_AfterUpdate_ShouldReturnCurrentVersion() {
        Seller updated = sellerService.updateSeller(testSeller.getId(), "Ivan Petrovich");

        Seller current = sellerService.getCurrentSeller(testSeller.getId());

        assertThat(current).isNotNull();
        assertThat(current.getId()).isEqualTo(updated.getId());
        assertThat(current.getName()).isEqualTo("Ivan Petrovich");
        assertThat(current.getCurrent()).isTrue();
        assertThat(current.getVersion()).isEqualTo(2);
    }

    @Test
    void getAllSellers_ShouldReturnAllSellers() {
        sellerService.createSeller("Maria", "maria@mail.ru");
        sellerService.createSeller("Petr", "petr@mail.ru");

        List<Seller> allSellers = sellerService.getAllSellers();

        assertThat(allSellers).hasSize(3);
        assertThat(allSellers).extracting(Seller::getName)
            .containsExactlyInAnyOrder("Ivan Petrov", "Maria", "Petr");
    }

    @Test
    void getAllSellers_WhenNoSellers_ShouldReturnEmptyList() {
        sellerRepository.deleteAll();
        List<Seller> allSellers = sellerService.getAllSellers();
        assertThat(allSellers).isEmpty();
    }

    @Test
    void getAllSellers_ShouldIncludeOldVersions() {
        sellerService.updateSeller(testSeller.getId(), "New Name");

        List<Seller> allSellers = sellerService.getAllSellers();

        assertThat(allSellers).hasSize(2);
        assertThat(allSellers).extracting(Seller::getName)
            .containsExactlyInAnyOrder("Ivan Petrov", "New Name");
    }

    @Test
    void updateSeller_ShouldCreateNewVersion() {
        Seller updated = sellerService.updateSeller(testSeller.getId(), "Ivan Petrovich");

        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isNotEqualTo(testSeller.getId());
        assertThat(updated.getName()).isEqualTo("Ivan Petrovich");
        assertThat(updated.getVersion()).isEqualTo(2);
        assertThat(updated.getCurrent()).isTrue();
        assertThat(updated.getValidTo()).isNull();
        assertThat(updated.getOriginalId()).isEqualTo(testSeller.getId());
        assertThat(updated.getContactInfo()).isEqualTo(testSeller.getContactInfo());
        assertThat(updated.getRegistrationDate()).isEqualTo(testSeller.getRegistrationDate());
    }

    @Test
    void updateSeller_ShouldDeactivateOldVersion() {
        sellerService.updateSeller(testSeller.getId(), "New Name");

        Seller oldVersion = sellerRepository.findById(testSeller.getId()).orElse(null);
        assertThat(oldVersion).isNotNull();
        assertThat(oldVersion.getCurrent()).isFalse();
        assertThat(oldVersion.getValidTo()).isNotNull();
        assertThat(oldVersion.getName()).isEqualTo("Ivan Petrov");
    }

    @Test
    void updateSeller_MultipleUpdates_ShouldCreateMultipleVersions() {
        Seller v2 = sellerService.updateSeller(testSeller.getId(), "Name V2");
        Seller v3 = sellerService.updateSeller(testSeller.getId(), "Name V3");

        assertThat(v2.getVersion()).isEqualTo(2);
        assertThat(v3.getVersion()).isEqualTo(3);

        assertThat(sellerRepository.findById(testSeller.getId()).get().getCurrent()).isFalse();
        assertThat(sellerRepository.findById(v2.getId()).get().getCurrent()).isFalse();
        assertThat(sellerRepository.findById(v3.getId()).get().getCurrent()).isTrue();
    }

    @Test
    void updateSeller_ShouldPreserveContactInfoAndRegistrationDate() {
        Seller updated = sellerService.updateSeller(testSeller.getId(), "New Name");

        assertThat(updated.getContactInfo()).isEqualTo(testSeller.getContactInfo());
        assertThat(updated.getRegistrationDate()).isEqualTo(testSeller.getRegistrationDate());
    }

    @Test
    void updateSeller_WhenNotFound_ShouldThrowException() {
        assertThatThrownBy(() -> sellerService.updateSeller(99999, "New Name"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Seller not found with id: 99999");
    }

    @Test
    void deleteSeller_ShouldRemoveSeller() {
        sellerService.deleteSeller(testSeller.getId());

        assertThat(sellerRepository.findById(testSeller.getId())).isEmpty();
        assertThat(sellerRepository.count()).isEqualTo(0);
    }

    @Test
    void deleteSeller_WhenNotFound_ShouldThrowException() {
        assertThatThrownBy(() -> sellerService.deleteSeller(99999))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getBestSeller_ShouldReturnSellerWithHighestTotalAmount() {
        Seller seller2 = sellerService.createSeller("Maria", "maria@mail.ru");
        Seller seller3 = sellerService.createSeller("Petr", "petr@mail.ru");

        transactionService.createTransaction(testSeller.getId(), "CARD", 1000);
        transactionService.createTransaction(testSeller.getId(), "CASH", 500);
        transactionService.createTransaction(seller2.getId(), "CARD", 3000);
        transactionService.createTransaction(seller3.getId(), "CARD", 500);

        Seller best = sellerService.getBestSeller();

        assertThat(best).isNotNull();
        assertThat(best.getName()).isEqualTo("Maria");
        assertThat(best.getId()).isEqualTo(seller2.getId());
    }

    @Test
    void getBestSeller_WhenOnlyOneSeller_ShouldReturnThatSeller() {
        transactionService.createTransaction(testSeller.getId(), "CARD", 1000);
        Seller best = sellerService.getBestSeller();
        assertThat(best).isNotNull();
        assertThat(best.getName()).isEqualTo("Ivan Petrov");
    }

    @Test
    void getBestSeller_WhenNoTransactions_ShouldThrowException() {
        assertThatThrownBy(() -> sellerService.getBestSeller())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("No sellers with transactions");
    }

    @Test
    void getBestSeller_WhenEqualAmounts_ShouldReturnOneOfThem() {
        Seller seller2 = sellerService.createSeller("Maria", "maria@mail.ru");

        transactionService.createTransaction(testSeller.getId(), "CARD", 1000);
        transactionService.createTransaction(seller2.getId(), "CARD", 1000);

        Seller best = sellerService.getBestSeller();

        assertThat(best).isNotNull();
        assertThat(best.getName()).isIn("Ivan Petrov", "Maria");
    }

    @Test
    void getSellersWithLowRevenue_ShouldFilterCorrectly() {
        Seller seller2 = sellerService.createSeller("Maria", "maria@mail.ru");
        Seller seller3 = sellerService.createSeller("Petr", "petr@mail.ru");

        transactionService.createTransaction(testSeller.getId(), "CARD", 500);
        transactionService.createTransaction(seller2.getId(), "CARD", 1500);
        transactionService.createTransaction(seller3.getId(), "CARD", 800);

        List<Seller> lowRevenue = sellerService.getSellersWithLowRevenue(1000);

        assertThat(lowRevenue).hasSize(2);
        assertThat(lowRevenue).extracting(Seller::getName)
            .containsExactlyInAnyOrder("Ivan Petrov", "Petr");
    }

    @Test
    void getSellersWithLowRevenue_WhenNoSellersBelowThreshold_ShouldReturnEmptyList() {
        transactionService.createTransaction(testSeller.getId(), "CARD", 2000);
        List<Seller> lowRevenue = sellerService.getSellersWithLowRevenue(1000);
        assertThat(lowRevenue).isEmpty();
    }

    @Test
    void getSellersWithLowRevenue_WhenAllBelowThreshold_ShouldReturnAll() {
        Seller seller2 = sellerService.createSeller("Maria", "maria@mail.ru");

        transactionService.createTransaction(testSeller.getId(), "CARD", 100);
        transactionService.createTransaction(seller2.getId(), "CARD", 200);

        List<Seller> lowRevenue = sellerService.getSellersWithLowRevenue(1000);
        assertThat(lowRevenue).hasSize(2);
    }

    @Test
    void getSellersWithLowRevenue_WhenZeroThreshold_ShouldReturnOnlyWithZeroRevenue() {
        Seller seller2 = sellerService.createSeller("Maria", "maria@mail.ru");
        transactionService.createTransaction(testSeller.getId(), "CARD", 100);

        List<Seller> lowRevenue = sellerService.getSellersWithLowRevenue(0);

        assertThat(lowRevenue).hasSize(1);
        assertThat(lowRevenue.get(0).getName()).isEqualTo("Maria");
    }

    @Test
    void getSellerHistory_ShouldReturnAllVersions() {
        sellerService.updateSeller(testSeller.getId(), "Version 2");
        sellerService.updateSeller(testSeller.getId(), "Version 3");

        List<Seller> history = sellerService.getSellerHistory(testSeller.getId());

        assertThat(history).hasSize(3);
        assertThat(history).extracting(Seller::getVersion)
            .containsExactly(1, 2, 3);
        assertThat(history).extracting(Seller::getName)
            .containsExactly("Ivan Petrov", "Version 2", "Version 3");
    }

    @Test
    void getSellerHistory_WhenNoHistory_ShouldReturnSingleVersion() {
        List<Seller> history = sellerService.getSellerHistory(testSeller.getId());

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getName()).isEqualTo("Ivan Petrov");
        assertThat(history.get(0).getVersion()).isEqualTo(1);
    }

    @Test
    void getSellerHistory_WhenSellerNotFound_ShouldThrowException() {
        assertThatThrownBy(() -> sellerService.getSellerHistory(99999))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void versioning_ShouldMaintainCorrectOrder() {
        Seller v1 = testSeller;
        Seller v2 = sellerService.updateSeller(v1.getId(), "Version 2");
        Seller v3 = sellerService.updateSeller(v1.getId(), "Version 3");

        List<Seller> history = sellerService.getSellerHistory(v1.getId());

        assertThat(history.get(0).getVersion()).isEqualTo(1);
        assertThat(history.get(1).getVersion()).isEqualTo(2);
        assertThat(history.get(2).getVersion()).isEqualTo(3);

        assertThat(history.get(0).getValidTo()).isNotNull();
        assertThat(history.get(1).getValidTo()).isNotNull();
        assertThat(history.get(2).getValidTo()).isNull();
    }

    @Test
    void createAndUpdate_ShouldNotLoseData() {
        Seller seller = sellerService.createSeller("Test", "test@mail.ru");
        sellerService.updateSeller(seller.getId(), "Updated");

        Seller current = sellerService.getCurrentSeller(seller.getId());

        assertThat(current.getContactInfo()).isEqualTo("test@mail.ru");
        assertThat(current.getOriginalId()).isEqualTo(seller.getId());
    }
}