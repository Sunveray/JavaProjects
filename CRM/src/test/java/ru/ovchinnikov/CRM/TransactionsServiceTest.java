package ru.ovchinnikov.CRM;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

@SpringBootTest(classes = CrmApplication.class)
@ActiveProfiles("test")
@Transactional
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SellerRepository sellerRepository;

    private Seller testSeller;
    private Integer testSellerId;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        sellerRepository.deleteAll();
        testSeller = sellerService.createSeller("Ivan Petrov", "ivan@mail.ru");
        testSellerId = testSeller.getId();
    }

    @Test
    void createTransaction_WhenSellerNotFound_ShouldThrowException() {
        assertThatThrownBy(() -> transactionService.createTransaction(99999, "CARD", 500))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Seller not found with id: 99999");
    }

    @Test
    void createTransaction_WithNullPaymentType_ShouldSave() {
        Transaction transaction = transactionService.createTransaction(testSellerId, null, 500);

        assertThat(transaction).isNotNull();
        assertThat(transaction.getPaymentType()).isNull();
        assertThat(transaction.getId()).isNotNull();
    }

    @Test
    void createTransaction_WithZeroAmount_ShouldSave() {
        Transaction transaction = transactionService.createTransaction(testSellerId, "CARD", 0);

        assertThat(transaction).isNotNull();
        assertThat(transaction.getAmount()).isEqualTo(0);
        assertThat(transaction.getId()).isNotNull();
    }

    @Test
    void createTransaction_WithNegativeAmount_ShouldSave() {
        Transaction transaction = transactionService.createTransaction(testSellerId, "CARD", -100);

        assertThat(transaction).isNotNull();
        assertThat(transaction.getAmount()).isEqualTo(-100);
        assertThat(transaction.getId()).isNotNull();
    }

    @Test
    void createTransaction_WithLargeAmount_ShouldSave() {
        Transaction transaction = transactionService.createTransaction(testSellerId, "CARD", 1000000);

        assertThat(transaction).isNotNull();
        assertThat(transaction.getAmount()).isEqualTo(1000000);
        assertThat(transaction.getId()).isNotNull();
    }

    @Test
    void createTransaction_MultipleTransactions_ShouldHaveUniqueIds() {
        Transaction t1 = transactionService.createTransaction(testSellerId, "CARD", 100);
        Transaction t2 = transactionService.createTransaction(testSellerId, "CASH", 200);
        Transaction t3 = transactionService.createTransaction(testSellerId, "ONLINE", 300);

        assertThat(t1.getId()).isNotEqualTo(t2.getId());
        assertThat(t1.getId()).isNotEqualTo(t3.getId());
        assertThat(t2.getId()).isNotEqualTo(t3.getId());

        assertThat(transactionRepository.count()).isEqualTo(3);
    }

    @Test
    void createTransaction_ShouldSetCorrectSeller() {
        Seller anotherSeller = sellerService.createSeller("Maria Sidorova", "maria@mail.ru");

        Transaction transaction = transactionService.createTransaction(anotherSeller.getId(), "CARD", 1000);

        assertThat(transaction.getSeller().getId()).isEqualTo(anotherSeller.getId());
        assertThat(transaction.getSeller().getName()).isEqualTo("Maria Sidorova");
    }

    @Test
    void createTransaction_ShouldStoreDifferentPaymentTypes() {
        Transaction card = transactionService.createTransaction(testSellerId, "CARD", 100);
        Transaction cash = transactionService.createTransaction(testSellerId, "CASH", 200);
        Transaction online = transactionService.createTransaction(testSellerId, "ONLINE", 300);
        Transaction empty = transactionService.createTransaction(testSellerId, "", 400);

        assertThat(card.getPaymentType()).isEqualTo("CARD");
        assertThat(cash.getPaymentType()).isEqualTo("CASH");
        assertThat(online.getPaymentType()).isEqualTo("ONLINE");
        assertThat(empty.getPaymentType()).isEqualTo("");
    }

    @Test
    void getSellerTransactions_ShouldReturnAllTransactionsForSeller() {
        transactionService.createTransaction(testSellerId, "CARD", 100);
        transactionService.createTransaction(testSellerId, "CASH", 200);
        transactionService.createTransaction(testSellerId, "ONLINE", 300);

        List<Transaction> transactions = transactionService.getSellerTransactions(testSellerId);

        assertThat(transactions).hasSize(3);
        assertThat(transactions).extracting(Transaction::getAmount)
            .containsExactlyInAnyOrder(100, 200, 300);
    }

    @Test
    void getSellerTransactions_WhenNoTransactions_ShouldReturnEmptyList() {
        List<Transaction> transactions = transactionService.getSellerTransactions(testSellerId);
        assertThat(transactions).isEmpty();
    }

    @Test
    void getSellerTransactions_WhenSellerNotFound_ShouldThrowException() {
        assertThatThrownBy(() -> transactionService.getSellerTransactions(99999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Seller not found with id: 99999");
    }

    @Test
    void getSellerTransactions_ShouldNotIncludeOtherSellersTransactions() {
        Seller anotherSeller = sellerService.createSeller("Petr Ivanov", "petr@mail.ru");

        transactionService.createTransaction(testSellerId, "CARD", 100);
        transactionService.createTransaction(anotherSeller.getId(), "CASH", 500);
        transactionService.createTransaction(anotherSeller.getId(), "ONLINE", 300);

        List<Transaction> transactions = transactionService.getSellerTransactions(testSellerId);

        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getAmount()).isEqualTo(100);
    }

    @Test
    void getSellerTransactions_ShouldReturnInAnyOrder() {
        transactionService.createTransaction(testSellerId, "CARD", 300);
        transactionService.createTransaction(testSellerId, "CASH", 100);
        transactionService.createTransaction(testSellerId, "ONLINE", 200);

        List<Transaction> transactions = transactionService.getSellerTransactions(testSellerId);

        assertThat(transactions).extracting(Transaction::getAmount)
            .containsExactlyInAnyOrder(100, 200, 300);
    }

    @Test
    void getAllTransactions_ShouldReturnAllTransactions() {
        Seller seller2 = sellerService.createSeller("Maria", "maria@mail.ru");
        Seller seller3 = sellerService.createSeller("Petr", "petr@mail.ru");

        transactionService.createTransaction(testSellerId, "CARD", 100);
        transactionService.createTransaction(seller2.getId(), "CASH", 200);
        transactionService.createTransaction(seller3.getId(), "ONLINE", 300);

        List<Transaction> allTransactions = transactionService.getAllTransactions();

        assertThat(allTransactions).hasSize(3);
        assertThat(allTransactions).extracting(Transaction::getAmount)
            .containsExactlyInAnyOrder(100, 200, 300);
    }

    @Test
    void getAllTransactions_WhenNoTransactions_ShouldReturnEmptyList() {
        transactionRepository.deleteAll();
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        assertThat(allTransactions).isEmpty();
    }

    @Test
    void getTransactionById_WhenExists_ShouldReturnTransaction() {
        Transaction saved = transactionService.createTransaction(testSellerId, "CARD", 500);

        Transaction found = transactionService.getTransactionById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getAmount()).isEqualTo(500);
        assertThat(found.getPaymentType()).isEqualTo("CARD");
        assertThat(found.getSeller().getId()).isEqualTo(testSellerId);
    }

    @Test
    void getTransactionById_WhenNotExists_ShouldThrowException() {
        assertThatThrownBy(() -> transactionService.getTransactionById(99999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Transaction not found with id: 99999");
    }

    @Test
    void multipleTransactionsForSameSeller_ShouldAllBeRetrieved() {
        for (int i = 0; i < 10; i++) {
            transactionService.createTransaction(testSellerId, "CARD", i * 100);
        }

        List<Transaction> transactions = transactionService.getSellerTransactions(testSellerId);

        assertThat(transactions).hasSize(10);
        assertThat(transactions).extracting(Transaction::getAmount)
            .containsExactlyInAnyOrder(0, 100, 200, 300, 400, 500, 600, 700, 800, 900);
    }

    @Test
    void createTransaction_ShouldPersistToDatabase() {
        Transaction transaction = transactionService.createTransaction(testSellerId, "CARD", 750);

        Transaction found = transactionRepository.findById(transaction.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getAmount()).isEqualTo(750);
        assertThat(found.getPaymentType()).isEqualTo("CARD");
        assertThat(found.getSeller().getId()).isEqualTo(testSellerId);
    }

    @Test
    void getSellerTransactions_AfterSellerUpdate_ShouldStillWork() {
        Transaction original = transactionService.createTransaction(testSellerId, "CARD", 500);

        sellerService.updateSeller(testSellerId, "Updated Name");

        List<Transaction> transactions = transactionService.getSellerTransactions(testSellerId);

        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getId()).isEqualTo(original.getId());
        assertThat(transactions.get(0).getAmount()).isEqualTo(500);
    }

    @Test
    void createTransaction_WithSameSellerMultipleTimes_ShouldAllBeLinked() {
        Transaction t1 = transactionService.createTransaction(testSellerId, "CARD", 100);
        Transaction t2 = transactionService.createTransaction(testSellerId, "CASH", 200);
        Transaction t3 = transactionService.createTransaction(testSellerId, "ONLINE", 300);

        assertThat(t1.getSeller().getId()).isEqualTo(testSellerId);
        assertThat(t2.getSeller().getId()).isEqualTo(testSellerId);
        assertThat(t3.getSeller().getId()).isEqualTo(testSellerId);

        List<Transaction> transactions = transactionService.getSellerTransactions(testSellerId);
        assertThat(transactions).hasSize(3);
    }

    @Test
    void createTransaction_ShouldHandleMaxIntegerAmount() {
        Transaction transaction = transactionService.createTransaction(testSellerId, "CARD", Integer.MAX_VALUE);

        assertThat(transaction.getAmount()).isEqualTo(Integer.MAX_VALUE);

        Transaction found = transactionService.getTransactionById(transaction.getId());
        assertThat(found.getAmount()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void createTransaction_ShouldHandleMinIntegerAmount() {
        Transaction transaction = transactionService.createTransaction(testSellerId, "CARD", Integer.MIN_VALUE);

        assertThat(transaction.getAmount()).isEqualTo(Integer.MIN_VALUE);

        Transaction found = transactionService.getTransactionById(transaction.getId());
        assertThat(found.getAmount()).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void getSellerTransactions_ForDeletedSeller_ShouldThrowException() {
        sellerService.deleteSeller(testSellerId);

        assertThatThrownBy(() -> transactionService.getSellerTransactions(testSellerId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Seller not found with id: " + testSellerId);
    }


    @Test
    void getAllTransactions_ShouldIncludeAllSellersTransactions() {
        Seller seller2 = sellerService.createSeller("Maria", "maria@mail.ru");
        Seller seller3 = sellerService.createSeller("Petr", "petr@mail.ru");

        Transaction t1 = transactionService.createTransaction(testSellerId, "CARD", 100);
        Transaction t2 = transactionService.createTransaction(seller2.getId(), "CASH", 200);
        Transaction t3 = transactionService.createTransaction(seller3.getId(), "ONLINE", 300);

        List<Transaction> all = transactionService.getAllTransactions();

        assertThat(all).hasSize(3);
        assertThat(all).extracting(Transaction::getId)
            .containsExactlyInAnyOrder(t1.getId(), t2.getId(), t3.getId());
    }
}