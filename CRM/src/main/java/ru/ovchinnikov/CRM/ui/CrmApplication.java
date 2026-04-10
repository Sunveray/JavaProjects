package ru.ovchinnikov.CRM.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "ru.ovchinnikov.CRM.model")
@ComponentScan(basePackages = "ru.ovchinnikov.CRM")
@EnableJpaRepositories(basePackages = "ru.ovchinnikov.CRM.repositories")
public class CrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrmApplication.class, args);
	}
}
