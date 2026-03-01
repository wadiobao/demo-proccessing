package com.example.demo.mongo.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.mongo.entity.Content;
import com.example.demo.mongo.repository.ContentRepository;
import com.example.demo.mongo.service.iservice.IContentService;
import com.example.demo.utils.FileBasedKeywordExtractor;
import com.example.demo.utils.VectorUtils;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
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

	@Override
	public Content findOrCreateMetadata(String content, String owner) throws IOException {
		// 1. Check for EXACT match first (Fastest, avoids vector calc)
		Optional<Content> exactMatch = contentRepository.findFirstByContentAndOwner(content, owner);
		if (exactMatch.isPresent()) {
			System.out.println("Exact content match found for user: " + owner + ". Reusing existing record.");
			return exactMatch.get();
		}

		List<Double> embedding = vectorUtils.createVector(content);
		String detectedTopic = null;
		List<String> tags = null;

		// 2. Try Vector Search (Similarity) - Check for near-exact match first
		try {
			Content similar = searchSimilar(embedding, 1, owner);
			if (similar != null) {
				// If similarity is extremely high (e.g., > 0.95), return existing to avoid
				// duplicate save
				if (similar.getVectorSearchScore() >= 0.95) {
					System.out.println("Reusing existing content/topic (idempotent path) score: "
							+ similar.getVectorSearchScore() + " topic: " + similar.getTopic());
					return similar;
				}

				// Otherwise, just reuse metadata (Topic/Tags) but we will still save this new
				// instance
				detectedTopic = similar.getTopic();
				tags = similar.getTags();
				System.out.println("Reusing metadata from similar content (score: "
						+ similar.getVectorSearchScore() + "): " + detectedTopic);
			}
		} catch (Exception e) {
			System.err.println("Similarity check failed in ContentService: " + e.getMessage());
		}

		// 3. Try Local Keyword Extraction if similarity failed or wasn't strong enough
		if (detectedTopic == null) {
			try {
				tags = keywordExtractor.getTopKeywords(content, 10);
				if (!tags.isEmpty()) {
					detectedTopic = tags.get(0);
					System.out.println("Extracted local topic: " + detectedTopic);
				}
			} catch (Exception e) {
				System.err.println("Local keyword extraction failed in ContentService: " + e.getMessage());
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
