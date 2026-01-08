package com.example.demo.mongo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.dto.question.QuizRequest;
import com.example.demo.dto.question.UserAnswer;
import com.example.demo.mongo.entity.Content;
import com.example.demo.mongo.entity.UserResource;
import com.example.demo.mongo.repository.ContentRepository;
import com.example.demo.mongo.repository.UserResourceRepository;
import com.example.demo.mongo.service.iservice.IUserResourceService;
import com.example.demo.utils.GeneralUtils;
import com.example.demo.utils.IRTCalculator;
import com.example.demo.utils.SimilarityUtil;

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
	public void save(String id,String fileName,String pdfContent,String userName) {
		// 1. Tạo hoặc lấy Content từ collection content
		String contentId = generalUtils.sha256(pdfContent);
		Content content = contentRepository.findById(contentId)
				.orElse(Content.builder()
						.id(contentId)
						.content(pdfContent)
						.build());

		if (!contentRepository.existsById(contentId)) {
			contentRepository.save(content);
		}

		// 2. Tìm tài liệu hiện có của user và kiểm tra độ tương đồng
		List<UserResource> resourcesOfUser = userResourceRepository.findAllByUserName(userName);
		double bestSim = -1.0;
		UserResource mostSimilarResource = null;

		for (UserResource r : resourcesOfUser) {
			if (r.getContentIds() == null) {
				continue;
			}
			for (String cid : r.getContentIds()) {
				Content c = contentRepository.findById(cid).orElse(null);
				if (c == null || c.getContent() == null) {
					continue;
				}
				double sim = SimilarityUtil.similarity(pdfContent, c.getContent());
				if (sim > bestSim) {
					bestSim = sim;
					mostSimilarResource = r;
				}
			}
		}

		// 3. Nếu độ tương đồng >= 60% -> add thêm contentId vào resource hiện có
		if (mostSimilarResource != null && bestSim >= 0.6) {
			List<String> contentIds = mostSimilarResource.getContentIds();
			if (contentIds == null) {
				contentIds = new ArrayList<>();
			}
			if (!contentIds.contains(contentId)) {
				contentIds.add(contentId);
				mostSimilarResource.setContentIds(contentIds);
				userResourceRepository.save(mostSimilarResource);
			}
			return;
		}

		// 4. Nếu không có tài liệu nào tương đồng >= 60% -> tạo mới UserResource
		UserResource resource = userResourceRepository.findByTitle(fileName)
				.orElse(UserResource.builder()
						.id(id)
						.title(fileName)
						.contentIds(List.of(contentId))
						.history(new ArrayList<UserAnswer>())
						.userName(userName)
						.theta(0.0)
						.b(0.0)
						.build());

		userResourceRepository.save(resource);
	}

	@Override
	public StateResponse<Object> findByTitle(String author) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String id) {
		// TODO Auto-generated method stub
		
	}
	
	public void reviewAnswer(QuizRequest quizRequest) {
		UserResource userResource = userResourceRepository.findById(quizRequest.getId()).get();
		
		double[] reviewPoint = irtCalculator.reviewAnswer(quizRequest.getAnswers(), userResource.getTheta(),userResource.getHistory());
		double thetaNew = reviewPoint[0];
		double b_min = reviewPoint[1];
		double b_max = reviewPoint[2];
		double b = (b_max+b_min)/2;
		
		
		userResource.setTheta(thetaNew);
		userResource.setB(b);
		
		userResourceRepository.save(userResource);
	}

	@Override
	public boolean existsById(String id) {
		return userResourceRepository.existsById(id);
	}

}
