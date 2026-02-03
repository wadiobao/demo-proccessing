package com.example.demo.mongo.service;

import org.springframework.stereotype.Service;

import com.example.demo.mongo.dto.question.QuizRequest;
import com.example.demo.mongo.entity.UserResource;
import com.example.demo.mongo.repository.ContentRepository;
import com.example.demo.mongo.repository.UserResourceRepository;
import com.example.demo.mongo.service.iservice.IUserResourceService;
import com.example.demo.utils.GeneralUtils;
import com.example.demo.utils.IRTCalculator;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserResourceService implements IUserResourceService {

	UserResourceRepository userResourceRepository;

	ContentRepository contentRepository;

	IRTCalculator irtCalculator;

	GeneralUtils generalUtils;

	@Override
	@Transactional
	public void save(String fileName, String pdfContent, String userName) {
		UserResource u = userResourceRepository.findByUserName(userName)
				.orElse(UserResource
						.builder()
						.userName(userName)
						.build());

	}

	@Override
	public void delete(String id) {
		// TODO Auto-generated method stub

	}

	public void reviewAnswer(QuizRequest quizRequest) {
		UserResource userResource = userResourceRepository.findById(quizRequest.getId()).get();

		double[] reviewPoint = irtCalculator.reviewAnswer(quizRequest.getAnswers(), userResource.getTheta(),
				userResource.getHistory());
		double thetaNew = reviewPoint[0];
		double b_min = reviewPoint[1];
		double b_max = reviewPoint[2];
		double b = (b_max + b_min) / 2;

		userResource.setTheta(thetaNew);
		userResource.setB(b);

		userResourceRepository.save(userResource);
	}

	@Override
	public boolean existsById(String id) {
		return userResourceRepository.existsById(id);
	}

}
