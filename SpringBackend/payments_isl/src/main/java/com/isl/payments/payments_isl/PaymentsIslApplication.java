package com.isl.payments.payments_isl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PaymentsIslApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentsIslApplication.class, args);
	}

}
