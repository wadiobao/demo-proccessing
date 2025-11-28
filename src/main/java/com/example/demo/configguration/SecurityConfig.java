package com.example.demo.configguration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;

import com.example.demo.enums.Role;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final String[] AUTH_END_POINTS = { "/user/register","/user/register/otp", "/auth/login", "/auth/introspect","/auth/logout","/auth/refresh" };
	private final String[] API_END_POINTS = { "/api/handlepdf", "/api/uploadv4", "/api/pdfs","/api/webhook/cloudinary" ,"/api/test/*",};
	private final String[] FORM_END_POINTS = {"/discussion","/discussion/*/forms","/discussion/form/*"};
	private final String[] FILE_END_POINTS = {"file/test","mail/donate"};
	
	@Value("${app.cors.allowed-origins}")
	private String cors;
	
	@Autowired
	private CustomJwtDecoder customJwtDecoder;
	
	@Autowired
	private CookieBearerTokenResolver bearerTokenResolver;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(request -> request.requestMatchers(HttpMethod.POST, AUTH_END_POINTS).permitAll()
				.requestMatchers(HttpMethod.GET, AUTH_END_POINTS).permitAll()
				.requestMatchers(HttpMethod.POST, API_END_POINTS).permitAll()
				.requestMatchers(HttpMethod.GET, API_END_POINTS).permitAll()
				.requestMatchers(HttpMethod.GET, FORM_END_POINTS).permitAll()
				.requestMatchers(HttpMethod.POST, FORM_END_POINTS).permitAll()
				.requestMatchers(HttpMethod.POST, FILE_END_POINTS).permitAll()
				.requestMatchers(HttpMethod.GET,FILE_END_POINTS).permitAll()
				.requestMatchers(HttpMethod.GET, "/user").hasRole(Role.ADMIN.name())// hasAuthority("ROLE_ADMIN")
				.anyRequest().authenticated());

		http.oauth2ResourceServer(oauth2 -> oauth2.bearerTokenResolver(bearerTokenResolver).jwt(
				jwtconfig -> jwtconfig.decoder(customJwtDecoder).jwtAuthenticationConverter(authenticationConverter()))
				.authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
		http.csrf(httpconfig -> httpconfig.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.ignoringRequestMatchers(AUTH_END_POINTS)
				.ignoringRequestMatchers(API_END_POINTS)
				.ignoringRequestMatchers(FILE_END_POINTS)
				.ignoringRequestMatchers(FORM_END_POINTS));
		
		http.logout(t -> t.disable());
				
		http.cors(t -> t.configurationSource(request -> {
			CorsConfiguration configuration = new CorsConfiguration();
			configuration.setAllowedOrigins(List.of(cors));
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
			configuration.setAllowedHeaders(List.of("*"));
			configuration.setExposedHeaders(List.of("Access-Token","Refresh-Token"));
			configuration.setAllowCredentials(true);
			return configuration;
		}));
		return http.build();
	}

	@Bean
	JwtAuthenticationConverter authenticationConverter() {
		JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
		authoritiesConverter.setAuthorityPrefix("ROLE_");
		JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
		authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
		return authenticationConverter;
	}


	@Bean
	PasswordEncoder encoder() {
		return new BCryptPasswordEncoder(10);
	}

}
