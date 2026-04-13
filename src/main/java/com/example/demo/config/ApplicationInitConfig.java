package com.example.demo.configguration;

import java.util.Date;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.sql.entity.Admin;
import com.example.demo.sql.entity.NormalUser;
import com.example.demo.sql.entity.Role;
import com.example.demo.sql.entity.Tier;
import com.example.demo.sql.repository.RoleRepository;
import com.example.demo.sql.repository.TierRepository;
import com.example.demo.sql.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
	
	PasswordEncoder encoder;

	@Bean
	ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository, TierRepository tierRepository) {
		return args -> {
			// Get ADMIN role assuming Flyway has already inserted it
			Role adminRole = roleRepository.findById("ADMIN").orElseThrow(() -> new RuntimeException("ADMIN role not found in database. Check Flyway migrations."));

			if(userRepository.findByUserName("admin").isEmpty()) {
				Admin admin = Admin.builder()
								.userName("admin")
								.password(encoder.encode("admin"))
								.email("dumabao69@gmail.com")
								.date(new Date())
								.role(adminRole)
								.employeeId("SYS-ADMIN-001")
								.department("IT")
								.build();
						
				userRepository.save(admin);
				log.warn("created default admin specializing in Inheritance structure");
			}
			
			Role userRole = roleRepository.findById("USER").orElseThrow(() -> new RuntimeException("USER role not found in database. Check Flyway migrations."));

			Tier userTier = tierRepository.findById("USER").orElseThrow(() -> new RuntimeException("USER tier not found in database. Check Flyway migrations."));
			
			if(userRepository.findByUserName("userdemo123").isEmpty()) {
				NormalUser user = NormalUser.builder()
								.userName("userdemo123")
								.password(encoder.encode("123123123"))
								.email("dumabao691@gmail.com")
								.date(new Date())
								.role(userRole)
								.currentTier(userTier)
								.build();
						
				userRepository.save(user);
				log.warn("created default user specializing in Inheritance structure");
			}
		};
	}
}
