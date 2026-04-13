package com.example.demo.configguration;

import java.text.ParseException;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.example.demo.sql.dto.Introspect;
import com.example.demo.sql.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;

@Component
public class CustomJwtDecoder implements JwtDecoder{

	@Value("${demo.secret.key}")
	private String SIGN_KEY;
	
	@Autowired
	private AuthenticationService authenticationService;
	
	private NimbusJwtDecoder decoder = null;
	
	
	@Override
	public Jwt decode(String token) throws JwtException {
		
		try {
			var response = authenticationService.introspect(Introspect.builder().token(token).build());
			if(!response.isValid()) {
				throw new JwtException("Invalid token");
			}
		} catch (JOSEException | ParseException e) {
			throw new JwtException(e.getMessage());
		}
		if(Objects.isNull(decoder)) {
			SecretKeySpec secretKeySpec = new  SecretKeySpec(SIGN_KEY.getBytes(), "HS512");
			decoder = NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS512).build();
		}
		
		return decoder.decode(token);
	}

}
