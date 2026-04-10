package ru.ovchinnikov.CRM;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
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

@SpringBootTest(classes = CrmApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class SellerControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Seller testSeller;
    private Integer testSellerId;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        sellerRepository.deleteAll();
        testSeller = sellerService.createSeller("Ivan Petrov", "ivan@mail.ru");
        testSellerId = testSeller.getId();

        transactionService.createTransaction(testSellerId, "CARD", 1000);
        transactionService.createTransaction(testSellerId, "CASH", 500);
    }

    @Test
    void getSellerById_WhenNotExists_ShouldReturnNotFound() {
        ResponseEntity<Seller> response = restTemplate.getForEntity("/sellers/99999", Seller.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getSellerById_WithNegativeId_ShouldReturnNotFound() {
        ResponseEntity<Seller> response = restTemplate.getForEntity("/sellers/-1", Seller.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getBestSeller_WhenNoTransactions_ShouldReturnNotFound() {
        transactionRepository.deleteAll();

        ResponseEntity<Seller> response = restTemplate.getForEntity("/sellers/best", Seller.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getLowRevenueSellers_WithNegativeThreshold_ShouldReturnEmptyList() {
        ResponseEntity<Seller[]> response = restTemplate.getForEntity(
            "/sellers/low-revenue?maxAmount=-100", Seller[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    @Test
    void createSeller_ShouldReturnCreated() {
        Seller newSeller = new Seller();
        newSeller.setName("Petr Ivanov");
        newSeller.setContactInfo("petr@mail.ru");

        ResponseEntity<Seller> response = restTemplate.postForEntity("/sellers", newSeller, Seller.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Petr Ivanov");
        assertThat(response.getBody().getContactInfo()).isEqualTo("petr@mail.ru");
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getHeaders().getLocation().toString()).contains("/sellers/");
    }

    @Test
    void createSeller_WithNullContactInfo_ShouldHandleError() {
        Seller invalidSeller = new Seller();
        invalidSeller.setName("Test User");
        invalidSeller.setContactInfo(null);

        ResponseEntity<Seller> response = restTemplate.postForEntity("/sellers", invalidSeller, Seller.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getSellerHistory_WhenNotFound_ShouldReturnNotFound() {
        ResponseEntity<Seller[]> response = restTemplate.getForEntity(
            "/sellers/99999/history", Seller[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}