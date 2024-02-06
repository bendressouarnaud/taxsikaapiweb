package com.ankk.tasikaapiweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {"com.ankk.taxsika.models"})
public class TasikaapiwebApplication {

	public static void main(String[] args) {
		SpringApplication.run(TasikaapiwebApplication.class, args);
	}

}
