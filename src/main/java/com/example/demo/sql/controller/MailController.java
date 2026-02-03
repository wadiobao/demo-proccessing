package com.example.demo.sql.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.service.iservice.IOTPMailService;

import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequestMapping("/api/v1/mail")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MailController {
	IOTPMailService mailService;

	@PostMapping("/donate")
	public ResponseEntity<StateResponse<Object>> sendMail(@RequestParam String name, @RequestParam String note,
			@RequestParam MultipartFile file)
			throws IOException, MessagingException {
		return ResponseEntity.ok(mailService.sendDonatetoMyMail(name, note, file));
	}

	@PostMapping("/send-bug")
	public ResponseEntity<StateResponse<Object>> sendBug(@RequestParam String name, @RequestParam String note)
			throws IOException, MessagingException {
		return ResponseEntity.ok(mailService.sendBugtoMyMail(name, note));
	}
}
