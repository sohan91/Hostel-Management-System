package com.example.HostelManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.HostelManagement")
public class HostelManagementApplication {
public static void main(String[] args) {
		SpringApplication.run(HostelManagementApplication.class, args);
	}
}
