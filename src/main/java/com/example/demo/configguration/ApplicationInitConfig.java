package com.example.demo.configguration;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.enums.Role;
import com.example.demo.sql.entity.User;
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
	ApplicationRunner applicationRunner(UserRepository repository) {
		return args -> {
			if(repository.findByUserName("admin").isEmpty()) {
				Set<String> roles = new HashSet<String>();
				roles.add(Role.ADMIN.name());
				
				User user = User.builder()
								.userName("admin")
								.password(encoder.encode("admin"))
								.email("dumabao69@gmail.com")
								.date(new Date())
								.roles(roles)
								.build();
						
				repository.save(user);
				
				log.warn("created default admin");
			}
		};
	}
}
