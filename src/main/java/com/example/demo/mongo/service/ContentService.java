package com.example.demo.mongo.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.mongo.dto.TopicAndTags;
import com.example.demo.mongo.entity.Content;
import com.example.demo.mongo.repository.ContentRepository;
import com.example.demo.mongo.service.iservice.IContentService;
import com.example.demo.mongo.service.quiz.GeminiAIService;
import com.example.demo.utils.VectorUtils;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class ContentService implements IContentService {
	
	ContentRepository contentRepository;
	VectorUtils vectorUtils;
	GeminiAIService aiService;

	@Override
	@Transactional
	public Content save(String content, String owner) throws IOException {
		List<Double> embedding = vectorUtils.createVector(content);
		TopicAndTags topicAndTags = aiService.detectTopicAndTags(content);

		Content c = Content.builder()
				.content(content)
				.owner(owner)
				.embedding(embedding)
				.topic(topicAndTags.getTopicId())
				.tags(topicAndTags.getTags())
				.build();
		return contentRepository.save(c);
	}

	@Override
	public Content searchSimilar(List<Double> queryVector, int limit, String username) {
		Content theMostSimilar = contentRepository.searchSimilar(queryVector, limit, username).get(0);
		Double score = theMostSimilar.getVectorSearchScore();
				
		if(score >= 0.7) {
			return theMostSimilar;
		}
		
		return Content.builder().build();
	}
	

}
