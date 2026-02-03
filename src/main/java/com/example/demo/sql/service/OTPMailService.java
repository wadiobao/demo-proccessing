package com.example.demo.sql.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.dto.user.UserRequest;
import com.example.demo.sql.dto.user.UserResponse;
import com.example.demo.sql.service.iservice.IOTPMailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OTPMailService implements IOTPMailService {
	
	@Value("${demo.donatefile.path}")
	String donateFile;
	
	@Autowired
	JavaMailSender javaMailSender;
	
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	@Autowired
	UserService userService;
	
	ObjectMapper mapper = new ObjectMapper();
	
	Map<String,String> otpCache = new ConcurrentHashMap<>();
	
	final String MY_MAIL = "dumabao69@gmail.com";
	
	@Override
	public String generateAndSendOtp(UserRequest request) {
		String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000,999999));
		otpCache.put(request.getEmail(), otp);
		String json = null;
		try {
			json = mapper.writeValueAsString(request);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		redisTemplate.opsForValue().set(request.getEmail(), otp, 3, TimeUnit.MINUTES);
		redisTemplate.opsForValue().set(request.getEmail()+otp, json, 3, TimeUnit.MINUTES);
		
		SimpleMailMessage mailMessage =  new SimpleMailMessage();
		mailMessage.setTo(request.getEmail());
		mailMessage.setSubject("Mã OTP");
		mailMessage.setText("Mã OTP của bạn là: "+otp+" \n Mã sẽ hết hạn sau 3 phút");
		javaMailSender.send(mailMessage);
		
		scheduleExpiry(request.getEmail(), 3, TimeUnit.MINUTES);
		
		return otp;
	}
	
	@Override
	public UserResponse verifyOtp(String email, String otp) {
		String dbOtp = redisTemplate.opsForValue().get(email);
		String json = redisTemplate.opsForValue().get(email+dbOtp);
		UserRequest request = null;
		try {
			 request = mapper.readValue(json, UserRequest.class);
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (otp != null && otp.equals(otp) && otp.equals(otpCache.get(email))) {
            redisTemplate.delete(email);
            redisTemplate.delete(email+dbOtp);
            return userService.registerUser(request);
        }
		return userService.registerUser(request);
	}
	
	private void scheduleExpiry(String email, long timeout, TimeUnit unit) {
        Executors.newSingleThreadScheduledExecutor()
            .schedule(() -> otpCache.remove(email), timeout, unit);
    }
	
	@Override
	public StateResponse<Object> sendDonatetoMyMail(String name,String note, MultipartFile file) throws IOException, MessagingException {
		 byte[] files = file.getBytes();
		 MimeMessage message = javaMailSender.createMimeMessage();
		 MimeMessageHelper helper = new MimeMessageHelper(message,true);
	     helper.setTo(MY_MAIL);
	     helper.setSubject("Yêu cầu file từ người dùng");
	     helper.setText("Bí danh người dùng: " + name +"\n"
	    		 		+"Ghi chú: " +note);
	     helper.addAttachment("file.pdf", new ByteArrayResource(files));
	     javaMailSender.send(message);
	     return StateResponse.builder().result(name).build();
	}
	
	@Override
	public StateResponse<Object> sendBugtoMyMail(String name,String note) throws IOException, MessagingException {
		 MimeMessage message = javaMailSender.createMimeMessage();
		 MimeMessageHelper helper = new MimeMessageHelper(message,true);
	     helper.setTo(MY_MAIL);
	     helper.setSubject("Bug từ người dùng");
	     helper.setText("Bí danh người dùng: " + name +"\n"
	    		 		+"Ghi chú: " +note);
	     javaMailSender.send(message);
	     return StateResponse.builder().result(name).build();
	}
}
