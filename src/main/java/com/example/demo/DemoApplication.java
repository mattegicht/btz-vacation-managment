package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.findByEmail("admin@admin.de") == null) {
				User admin = new User();
				admin.setName("Admin User");
				admin.setEmail("admin@admin.de");
				admin.setPassword(passwordEncoder.encode("password"));
				admin.setRole("admin");
				userRepository.save(admin);
				System.out.println("Admin user created: admin@admin.de / password");
			}
		};
	}

}
