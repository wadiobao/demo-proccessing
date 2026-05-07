package com.example.demo.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Value("${app.cors.allowed-origins}")
	private String cors;

	@Autowired
	private CustomJwtDecoder customJwtDecoder;

	@Autowired
	private CookieBearerTokenResolver bearerTokenResolver;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(request -> request
				// ---- ADMIN ENDPOINTS ----
				.requestMatchers(SecurityEndpoints.PRIVATE.ADMIN).hasRole("ADMIN")
				
				// ---- PUBLIC ENDPOINTS ----
				.requestMatchers(SecurityEndpoints.PUBLIC.all()).permitAll()
				
				// ---- ALL OTHER PRIVATE ENDPOINTS ----
				.anyRequest().authenticated());

		http.oauth2ResourceServer(oauth2 -> oauth2.bearerTokenResolver(bearerTokenResolver).jwt(
				jwtconfig -> jwtconfig.decoder(customJwtDecoder).jwtAuthenticationConverter(authenticationConverter()))
				.authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
		
		http.csrf(httpconfig -> httpconfig.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.ignoringRequestMatchers(SecurityEndpoints.PUBLIC.AUTH)
				.ignoringRequestMatchers(SecurityEndpoints.PUBLIC.QUIZ)
				.ignoringRequestMatchers(SecurityEndpoints.PUBLIC.FILE)
				.ignoringRequestMatchers(SecurityEndpoints.PUBLIC.INFRA));

		http.logout(t -> t.disable());

		http.cors(t -> t.configurationSource(request -> {
			CorsConfiguration configuration = new CorsConfiguration();
			configuration.setAllowedOrigins(List.of(cors));
			configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
			configuration.setAllowedHeaders(List.of("*"));
			configuration.setExposedHeaders(List.of("Access-Token", "Refresh-Token","access-token","refresh-token"));
			configuration.setAllowCredentials(true);
			return configuration;
		}));
		return http.build();
	}

	@Bean
	JwtAuthenticationConverter authenticationConverter() {
		JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
		// explicitly read from the 'scope' claim (matches what JwtUtils.buildScope writes)
		authoritiesConverter.setAuthoritiesClaimName("role");
		// buildScope now stores bare role names; Spring's hasRole() appends ROLE_ internally
		authoritiesConverter.setAuthorityPrefix("ROLE_");
		JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
		authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
		return authenticationConverter;
	}



}
