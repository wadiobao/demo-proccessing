package com.example.demo.config;

import java.util.Date;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.modules.identity.domain.model.Admin;
import com.example.demo.modules.identity.domain.model.NormalUser;
import com.example.demo.modules.identity.domain.model.Role;
import com.example.demo.modules.identity.domain.model.Tier;
import com.example.demo.modules.identity.domain.repository.IRoleRepository;
import com.example.demo.modules.identity.domain.repository.ITierRepository;
import com.example.demo.modules.identity.domain.repository.IUserRepository;

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
	ApplicationRunner applicationRunner(IUserRepository userRepo, IRoleRepository roleRepo, ITierRepository tierRepo) {
		return args -> {
			// Get ADMIN role assuming Flyway has already inserted it
			Role adminRole = roleRepo.findById("ADMIN").orElseThrow(() -> new RuntimeException("ADMIN role not found in database. Check Flyway migrations."));

			if(userRepo.findByUserName("admin").isEmpty()) {
				Admin admin = Admin.builder()
								.userName("admin")
								.password(encoder.encode("admin"))
								.email("dumabao69@gmail.com")
								.date(new Date())
								.role(adminRole)
								.employeeId("SYS-ADMIN-001")
								.department("IT")
								.build();
						
				userRepo.save(admin);
				log.warn("created default admin specializing in Inheritance structure");
			}
			
			Role userRole = roleRepo.findById("USER").orElseThrow(() -> new RuntimeException("USER role not found in database. Check Flyway migrations."));

			Tier userTier = tierRepo.findById("MEMBER").orElseThrow(() -> new RuntimeException("MEMBER tier not found in database. Check Flyway migrations."));
			
			if(userRepo.findByUserName("userdemo123").isEmpty()) {
				NormalUser user = NormalUser.builder()
								.userName("userdemo123")
								.password(encoder.encode("123123123"))
								.email("dumabao691@gmail.com")
								.date(new Date())
								.role(userRole)
								.currentTier(userTier)
								.build();
						
				userRepo.save(user);
				log.warn("created default user specializing in Inheritance structure");
			}
		};
	}
}
