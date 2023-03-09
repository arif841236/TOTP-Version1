package com.indusnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

//  This is springboot entry class
@SpringBootApplication
@EnableSwagger2
public class OtpGenerateApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(OtpGenerateApplication.class, args);
	}
	
}
