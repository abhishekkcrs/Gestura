package com.serviceregistry.islserviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class IslserviceregistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(IslserviceregistryApplication.class, args);
	}

}
