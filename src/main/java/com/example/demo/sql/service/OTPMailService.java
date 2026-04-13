package com.example.demo.sql.service;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.sql.dto.user.UserRequest;
import com.example.demo.sql.dto.user.UserResponse;
import com.example.demo.sql.repository.UserRepository;
import com.example.demo.sql.service.iservice.IOTPMailService;
import com.example.demo.utils.GeneralUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OTPMailService implements IOTPMailService {

	@Value("${demo.donatefile.path}")
	@NonFinal
	String donateFile;
	JavaMailSender javaMailSender;
	RedisTemplate<String, String> redisTemplate;	
	UserRepository userRepository;
	UserService userService;
	GeneralUtils generalUtils;

	ObjectMapper mapper;

	final String MY_MAIL = "dumabao69@gmail.com";

	@Override
	public String generateAndSendOtp(UserRequest request) {
		String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
		String json = null;
		try {
			json = mapper.writeValueAsString(request);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		String hash = generalUtils.sha256(request.getEmail() + otp);
		log.info("Hash when generate "+hash);
		
		redisTemplate.opsForValue().set(hash, json, 3, TimeUnit.MINUTES);

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(request.getEmail());
		mailMessage.setSubject("Mã OTP");
		mailMessage.setText("Mã OTP của bạn là: " + otp + " \n Mã sẽ hết hạn sau 3 phút");
		javaMailSender.send(mailMessage);
		
		return otp;
	}

	@Override
	public UserResponse verifyOtp(String email, String otp) {
		
		String hash = generalUtils.sha256(email+otp);
		String dbOtp = redisTemplate.opsForValue().get(hash);
		UserRequest request = new UserRequest();
		
		try {
			log.info("Hash when verify "+hash);
			
			// Ngăn chặn lỗi double submit (Request thứ 1 tạo user, Request thứ 2 đến sau bị mất OTP trong Redis chặn lại)
			// Trả về lỗi USER_EXISTED thân thiện hơn thay vì INVALID_OTP
			if (userRepository.existsByEmail(email)) {
				throw new HandleException(ErrorCode.USER_EXISTED);
			}

			if (dbOtp == null) {
				throw new HandleException(ErrorCode.INVALID_OTP);
			}

			request = mapper.readValue(dbOtp, UserRequest.class);
			
			redisTemplate.delete(hash);
			log.info(request.toString());
			
		} catch (JsonMappingException e) {
				e.printStackTrace();
		} catch (JsonProcessingException e) {
				e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			redisTemplate.delete(hash);
		}
		
		return userService.registerUser(request);
		
	}

	@Override
	public StateResponse<Object> sendDonatetoMyMail(String name, String note, MultipartFile file)
			throws IOException, MessagingException {
		byte[] files = file.getBytes();
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(MY_MAIL);
		helper.setSubject("Yêu cầu file từ người dùng");
		helper.setText("Bí danh người dùng: " + name + "\n"
				+ "Ghi chú: " + note);
		helper.addAttachment("file.pdf", new ByteArrayResource(files));
		javaMailSender.send(message);
		return StateResponse.builder().result(name).build();
	}

	@Override
	public StateResponse<Object> sendBugtoMyMail(String name, String note) throws IOException, MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(MY_MAIL);
		helper.setSubject("Bug từ người dùng");
		helper.setText("Bí danh người dùng: " + name + "\n"
				+ "Ghi chú: " + note);
		javaMailSender.send(message);
		return StateResponse.builder().result(name).build();
	}

	@Override
	public String sendForgotPasswordOtp(String email) {
		String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
		
		redisTemplate.opsForValue().set(email + "_RESET", otp, 5, TimeUnit.MINUTES);

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(email);
		mailMessage.setSubject("Mã khôi phục mật khẩu");
		mailMessage.setText("Mã OTP để khôi phục mật khẩu của bạn là: " + otp + " \n Mã sẽ hết hạn sau 5 phút");
		
		javaMailSender.send(mailMessage);

		return otp;
	}

	@Override
	public void verifyOtpForgotPassword(String email, String otp) {
		String dbOtp = redisTemplate.opsForValue().get(email + "_RESET");
		if (dbOtp == null || !otp.equals(dbOtp)) {
			throw new HandleException(ErrorCode.INVALID_OTP);
		}
		redisTemplate.delete(email + "_RESET");
	}
}
