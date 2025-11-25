package com.example.CineBook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CineBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(CineBookApplication.class, args);
	}

}
