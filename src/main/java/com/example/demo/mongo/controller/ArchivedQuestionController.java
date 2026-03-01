package com.example.demo.mongo.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.enums.ErrorCode;
import com.example.demo.enums.Role;
import com.example.demo.exception.HandleException;
import com.example.demo.mongo.entity.ArchivedQuestion;
import com.example.demo.mongo.service.iservice.IArchivedQuestionService;

@RestController
@RequestMapping("/api/v1/mongo")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class ArchivedQuestionController {
	
	@Autowired
	private IArchivedQuestionService iPdfStoreService;
	
	@PostMapping
	public ArchivedQuestion create(@RequestBody ArchivedQuestion pdfStore) throws Exception {
		return iPdfStoreService.save(pdfStore);
	}
	
	@GetMapping("/author")
	public StateResponse<Object> getAuthorPdf(@RequestParam String author,Authentication authentication) {
		String username = authentication.getName();
		Collection<? extends GrantedAuthority> role = authentication.getAuthorities();
		List<String> roles = new ArrayList<String>();
		for (GrantedAuthority grantedAuthority : role) {
			roles.add(grantedAuthority.getAuthority());
		}
		if(!author.equals(username) && !roles.contains("ROLE_"+Role.ADMIN.name())) {
			throw new HandleException(ErrorCode.UNAUTHORIZED);
		}
		return iPdfStoreService.findByAuthor(author);
	}
	
	@GetMapping("/all")
	public List<ArchivedQuestion> getAll() {
		return iPdfStoreService.findAll();
	}
}
