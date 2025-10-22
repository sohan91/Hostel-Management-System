package com.example.HostelManagement;

import com.example.HostelManagement.dao.AdminDAO;
import com.example.HostelManagement.entities.admin.Admin;
import com.example.HostelManagement.service.AdminService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HostelManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(HostelManagementApplication.class, args);

//	}
//	@Bean
//	public CommandLineRunner commandLineRunner(AdminService service) {
//		return args -> {
//			System.out.println("Creating object...");
//			Admin admin = new Admin("Sohan", "prasad@gmail.com", "Prasad@223", "3934734747", "Hyderabad");
//			System.out.println("Saving object...");
//			service.saveAdmin(admin);
//			System.out.println("Completed");
//		};
//	}
	}
}
