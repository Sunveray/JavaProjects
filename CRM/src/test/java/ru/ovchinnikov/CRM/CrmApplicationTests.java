package ru.ovchinnikov.CRM;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.ovchinnikov.CRM.model.Seller;
import ru.ovchinnikov.CRM.model.SellerService;
import ru.ovchinnikov.CRM.repositories.SellerRepository;
import ru.ovchinnikov.CRM.ui.CrmApplication;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest (classes = CrmApplication.class)
@ActiveProfiles("test")
class CrmApplicationTests {

	@Autowired
	private SellerRepository sellerRepository;

	@Autowired
    private SellerService sellerService;

	@Test
	void contextLoads() {
	}

	@Test
    public void AddSellerTest() {
		if (sellerRepository.count() == 0) {
			Seller s1 = new Seller();
			s1.setName("Иван Петров");
			s1.setContactInfo("ivan@mail.ru");
			s1.setRegistrationDate(LocalDateTime.now());
			sellerRepository.save(s1);

			Seller saved = sellerRepository.save(s1);

			assertThat(saved.getId()).isNotNull();
        	assertThat(saved.getName()).isEqualTo("Иван Петров");
        	assertThat(saved.getContactInfo()).isEqualTo("ivan@mail.ru");

			assertThat(sellerRepository.count()).isEqualTo(1);

			Seller found = sellerRepository.findById(saved.getId()).orElse(null);
        	assertThat(found).isNotNull();
        	assertThat(found.getName()).isEqualTo("Иван Петров");
		}
	}

	@Test
    void createSellerEmptyName() {
        assertThatThrownBy(() -> sellerService.createSeller("", "test@mail.ru"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Name is required");
    }

	@Test
    void updateSeller_ShouldCreateNewVersion() {
        Seller original = sellerService.createSeller("Иван", "ivan@mail.ru");
        String newName = "Иван Петрович";

        Seller updated = sellerService.updateSeller(original.getId(), original.getContactInfo());

        assertThat(updated.getId()).isNotEqualTo(original.getId());
        assertThat(updated.getName()).isEqualTo(newName);
        assertThat(updated.getVersion()).isEqualTo(2);
        assertThat(updated.getCurrent()).isTrue();
        assertThat(updated.getValidTo()).isNull();
        assertThat(updated.getOriginalId()).isEqualTo(original.getId());

        Seller oldVersion = sellerRepository.findById(original.getId()).get();
        assertThat(oldVersion.getCurrent()).isFalse();
        assertThat(oldVersion.getValidTo()).isNotNull();
    }

	@Test
	void bestSeller(){
		sellerService.
	}
}
