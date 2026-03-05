package com.example.demo.mongo.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.mongo.entity.Content;
import com.example.demo.mongo.repository.ContentRepository;
import com.example.demo.mongo.service.iservice.IContentService;
import com.example.demo.utils.FileBasedKeywordExtractor;
import com.example.demo.utils.VectorUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for analyzing and managing document content metadata.
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ContentService implements IContentService {

	ContentRepository contentRepository;
	VectorUtils vectorUtils;
	FileBasedKeywordExtractor keywordExtractor;

	@Override
	@Transactional
	public Content save(String content, String owner) throws IOException {
		Content metadata = findOrCreateMetadata(content, owner);
		if (metadata.getId() != null) {
			return metadata; // Already exists
		}
		return contentRepository.save(metadata);
	}

	/**
	 * Finds existing Content metadata based on text similarity or creates a new
	 * block via AI analysis.
	 *
	 * @param content full text of the document / nội dung văn bản gốc
	 * @param owner   identification of the owner / người sở hữu tài liệu
	 * @return Content metadata (AI-enriched) / thông tin Metadata (đã qua AI xử lý)
	 * @throws IOException if AI analysis fails / lỗi nếu phân tích AI thất bại
	 */
	@Override
	public Content findOrCreateMetadata(String content, String owner) throws IOException {
		// 1. Check for EXACT match first (Fastest, avoids vector calc)
		Optional<Content> exactMatch = contentRepository.findFirstByContentAndOwner(content, owner);
		if (exactMatch.isPresent()) {
			log.info("Exact content match found for user: {}. Reusing existing record.", owner);
			return exactMatch.get();
		}

		List<Double> embedding = vectorUtils.createVector(content);
		String detectedTopic = null;
		List<String> tags = null;

		// 2. Try Vector Search (Similarity) - Check for near-exact match first
		try {
			Content similar = searchSimilar(embedding, 1, owner);
			if (similar != null) {
				if (similar.getVectorSearchScore() >= 0.95) {
					log.info("Reusing existing content/topic (idempotent path) score: {} topic: {}",
							similar.getVectorSearchScore(), similar.getTopic());
					return similar;
				}
				detectedTopic = similar.getTopic();
				tags = similar.getTags();
				log.info("Reusing metadata from similar content (score: {}): {}",
						similar.getVectorSearchScore(), detectedTopic);
			}
		} catch (Exception e) {
			log.error("Similarity check failed in ContentService: {}", e.getMessage(), e);
		}

		// 3. Try Local Keyword Extraction if similarity failed or wasn't strong enough
		if (detectedTopic == null) {
			try {
				tags = keywordExtractor.getTopKeywords(content, 10);
				if (!tags.isEmpty()) {
					detectedTopic = tags.get(0);
					log.info("Extracted local topic: {}", detectedTopic);
				}
			} catch (Exception e) {
				log.error("Local keyword extraction failed in ContentService: {}", e.getMessage(), e);
			}
		}

		// 4. Fallback: Generic topic
		if (detectedTopic == null) {
			detectedTopic = "general:unknown";
			tags = new ArrayList<>();
		}

		// Return transient Content object (no ID)
		return Content.builder()
				.content(content)
				.owner(owner)
				.embedding(embedding)
				.topic(detectedTopic)
				.tags(tags)
				.build();
	}

	@Override
	public Content searchSimilar(List<Double> queryVector, int limit, String username) {
		List<Content> results = contentRepository.searchSimilar(queryVector, limit, username);
		if (results == null || results.isEmpty()) {
			return null;
		}

		Content theMostSimilar = results.get(0);
		Double score = theMostSimilar.getVectorSearchScore();

		if (score != null && score >= 0.7) {
			return theMostSimilar;
		}

		return null;
	}
}
