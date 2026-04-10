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
import ru.ovchinnikov.CRM.model.Transaction;
import ru.ovchinnikov.CRM.model.TransactionService;
import ru.ovchinnikov.CRM.repositories.SellerRepository;
import ru.ovchinnikov.CRM.repositories.TransactionRepository;
import ru.ovchinnikov.CRM.ui.CrmApplication;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = CrmApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class TransactionControllerTest {

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
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        sellerRepository.deleteAll();
        testSeller = sellerService.createSeller("Ivan Petrov", "ivan@mail.ru");
        testSellerId = testSeller.getId();
        testTransaction = transactionService.createTransaction(testSellerId, "CARD", 500);
    }

    @Test
    void getAllTransactions_ShouldReturnOk() {
        transactionService.createTransaction(testSellerId, "CASH", 300);
        transactionService.createTransaction(testSellerId, "ONLINE", 200);

        ResponseEntity<Transaction[]> response = restTemplate.getForEntity("/api/transactions", Transaction[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(3);
    }

    @Test
    void getAllTransactions_WhenNoTransactions_ShouldReturnEmptyList() {
        transactionRepository.deleteAll();

        ResponseEntity<Transaction[]> response = restTemplate.getForEntity("/api/transactions", Transaction[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    @Test
    void getTransactionById_WhenExists_ShouldReturnTransaction() {
        ResponseEntity<Transaction> response = restTemplate.getForEntity(
            "/api/transactions/" + testTransaction.getId(), Transaction.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testTransaction.getId());
        assertThat(response.getBody().getAmount()).isEqualTo(500);
        assertThat(response.getBody().getPaymentType()).isEqualTo("CARD");
    }

    @Test
    void getTransactionById_WhenNotExists_ShouldReturnNotFound() {
        ResponseEntity<Transaction> response = restTemplate.getForEntity("/api/transactions/99999", Transaction.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getTransactionById_WithNegativeId_ShouldReturnNotFound() {
        ResponseEntity<Transaction> response = restTemplate.getForEntity("/api/transactions/-1", Transaction.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createTransaction_ShouldReturnCreated() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format(
            "{\"sellerId\": %d, \"paymentType\": \"CARD\", \"amount\": 1000}",
            testSellerId
        );

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Transaction> response = restTemplate.postForEntity(
            "/api/transactions", request, Transaction.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualTo(1000);
        assertThat(response.getBody().getPaymentType()).isEqualTo("CARD");
        assertThat(response.getBody().getSeller().getId()).isEqualTo(testSellerId);
    }

    @Test
    void createTransaction_WithInvalidSellerId_ShouldReturnError() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"sellerId\": 99999, \"paymentType\": \"CARD\", \"amount\": 1000}";

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Transaction> response = restTemplate.postForEntity(
            "/api/transactions", request, Transaction.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void createTransaction_WithNegativeAmount_ShouldSave() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format(
            "{\"sellerId\": %d, \"paymentType\": \"CARD\", \"amount\": -500}",
            testSellerId
        );

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Transaction> response = restTemplate.postForEntity(
            "/api/transactions", request, Transaction.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualTo(-500);
    }

    @Test
    void createTransaction_WithZeroAmount_ShouldSave() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format(
            "{\"sellerId\": %d, \"paymentType\": \"CARD\", \"amount\": 0}",
            testSellerId
        );

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Transaction> response = restTemplate.postForEntity(
            "/api/transactions", request, Transaction.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualTo(0);
    }

    @Test
    void createTransaction_WithNullPaymentType_ShouldSave() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format(
            "{\"sellerId\": %d, \"paymentType\": null, \"amount\": 500}",
            testSellerId
        );

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Transaction> response = restTemplate.postForEntity(
            "/api/transactions", request, Transaction.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPaymentType()).isNull();
    }

    @Test
    void getSellerTransactions_ShouldReturnOk() {
        transactionService.createTransaction(testSellerId, "CASH", 300);
        transactionService.createTransaction(testSellerId, "ONLINE", 200);

        ResponseEntity<Transaction[]> response = restTemplate.getForEntity(
            "/api/transactions/seller/" + testSellerId, Transaction[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(3);
        assertThat(response.getBody()).extracting(Transaction::getAmount)
            .containsExactlyInAnyOrder(500, 300, 200);
    }

    @Test
    void getSellerTransactions_WhenNoTransactions_ShouldReturnEmptyList() {
        Seller newSeller = sellerService.createSeller("New Seller", "new@mail.ru");

        ResponseEntity<Transaction[]> response = restTemplate.getForEntity(
            "/api/transactions/seller/" + newSeller.getId(), Transaction[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    @Test
    void getSellerTransactions_WhenSellerNotFound_ShouldReturnNotFound() {
        ResponseEntity<Transaction[]> response = restTemplate.getForEntity(
            "/api/transactions/seller/99999", Transaction[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getSellerTransactions_ShouldNotIncludeOtherSellersTransactions() {
        Seller seller2 = sellerService.createSeller("Maria Sidorova", "maria@mail.ru");
        transactionService.createTransaction(seller2.getId(), "CARD", 2000);

        ResponseEntity<Transaction[]> response = restTemplate.getForEntity(
            "/api/transactions/seller/" + testSellerId, Transaction[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);
        assertThat(response.getBody()[0].getAmount()).isEqualTo(500);
    }

    @Test
    void createTransaction_WithLargeAmount_ShouldSave() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format(
            "{\"sellerId\": %d, \"paymentType\": \"CARD\", \"amount\": 1000000}",
            testSellerId
        );

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Transaction> response = restTemplate.postForEntity(
            "/api/transactions", request, Transaction.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualTo(1000000);
    }

    @Test
    void createTransaction_WithEmptyBody_ShouldReturnError() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>("{}", headers);

        ResponseEntity<Transaction> response = restTemplate.postForEntity(
            "/api/transactions", request, Transaction.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getTransactionById_AfterSellerDeleted_ShouldStillReturnTransaction() {
        sellerService.deleteSeller(testSellerId);

        ResponseEntity<Transaction> response = restTemplate.getForEntity(
            "/api/transactions/" + testTransaction.getId(), Transaction.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testTransaction.getId());
        assertThat(response.getBody().getAmount()).isEqualTo(500);
        assertThat(response.getBody().getSeller()).isNull();
    }

    @Test
    void getAllTransactions_AfterMultipleCreations_ShouldReturnAll() {
        Seller seller2 = sellerService.createSeller("Maria", "maria@mail.ru");

        transactionService.createTransaction(testSellerId, "CASH", 300);
        transactionService.createTransaction(seller2.getId(), "ONLINE", 400);

        ResponseEntity<Transaction[]> response = restTemplate.getForEntity("/api/transactions", Transaction[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(3);
    }
}
