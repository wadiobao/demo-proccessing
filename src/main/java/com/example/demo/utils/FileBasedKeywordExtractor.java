package com.example.demo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.example.demo.mongo.dto.quiz.FileCompareResult;

import jakarta.annotation.PostConstruct;

/**
 * Local text analyzer for keyword and topic extraction.
 * 
 * <p>
 * Sử dụng thuật toán phân tích tần suất từ (TF) và so sánh từ điển cục bộ
 * để trích xuất các từ khóa quan trọng và tính toán độ tương đồng giữa các tài
 * liệu.
 *
 * @since 1.0
 */
@Component
public class FileBasedKeywordExtractor {

	private Set<String> dictionary;
	private Set<String> stopWords;
	private int maxSyllables = 1;
	private static final Pattern TEXT_PATTERN = Pattern.compile("\"text\":\\s*\"([^\"]+)\"");

	@Value("classpath:instructions/words.txt")
	private Resource dictionaryResource;

	@Value("classpath:instructions/vietnamese-stopwords.txt")
	private Resource stopWordsResource;

	public FileBasedKeywordExtractor() {
		this.dictionary = new HashSet<>();
		this.stopWords = new HashSet<>();
	}

	@PostConstruct
	public void init() {
		try {
			if (dictionaryResource != null && dictionaryResource.exists()) {
				loadDictionary(dictionaryResource.getInputStream());
			}
			if (stopWordsResource != null && stopWordsResource.exists()) {
				loadStopWords(stopWordsResource.getInputStream());
			}
		} catch (IOException e) {
			System.err.println("Failed to load local dictionaries: " + e.getMessage());
		}
	}

	public void loadDictionary(InputStream inputStream) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				Matcher matcher = TEXT_PATTERN.matcher(line);
				if (matcher.find()) {
					String word = matcher.group(1).trim().toLowerCase();
					if (!word.isEmpty()) {
						dictionary.add(word);
						int len = word.split("\\s+").length;
						if (len > maxSyllables) {
							maxSyllables = len;
						}
					}
				}
			}
		}
	}

	public void loadStopWords(InputStream inputStream) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				String word = line.trim().toLowerCase();
				if (!word.isEmpty()) {
					stopWords.add(word);
				}
			}
		}
	}

	// --- PHẦN 1: NẠP TỪ ĐIỂN (Hỗ trợ String path cho legacy/main) ---
	public void loadDictionary(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		try (FileInputStream fis = new FileInputStream(file)) {
			loadDictionary(fis);
		}
	}

	public void loadStopWords(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		try (FileInputStream fis = new FileInputStream(file)) {
			loadStopWords(fis);
		}
	}

	// --- PHẦN 2: XỬ LÝ TÁCH TỪ ---
	/**
	 * Breaks down raw text into a list of meaningful syllables and compound words.
	 * 
	 * @param text raw input string / văn bản thô đầu vào
	 * @return list of extracted tokens / danh sách các từ đã tách
	 */
	public List<String> tokenize(String text) {
		List<String> tokens = new ArrayList<>();
		String cleanText = text
				.replaceAll("[^a-zA-Z0-9àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐ ]", " ");
		String[] syllables = cleanText.toLowerCase().trim().split("\\s+");
		int n = syllables.length;
		int i = 0;
		while (i < n) {
			String token = syllables[i];
			int step = 1;
			int maxLen = Math.min(maxSyllables, n - i);
			for (int len = maxLen; len >= 2; len--) {
				StringBuilder sb = new StringBuilder();
				for (int k = 0; k < len; k++) {
					sb.append(syllables[i + k]);
					if (k < len - 1) {
						sb.append(" ");
					}
				}
				String compound = sb.toString();
				if (dictionary.contains(compound)) {
					token = compound;
					step = len;
					break;
				}
			}
			tokens.add(token);
			i += step;
		}
		return tokens;
	}

	// --- PHẦN 3: ĐỌC FILE VÀ TRÍCH XUẤT MAP TẦN SUẤT ---

	public Map<String, Integer> getFrequencyMapFromFile(String filePath) throws IOException {
		Map<String, Integer> freqMap = new HashMap<>();
		File file = new File(filePath);

		if (!file.exists()) {
			throw new FileNotFoundException("File not found: " + filePath);
		}

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				List<String> tokens = tokenize(line);
				for (String token : tokens) {
					if (token.length() > 1 && !stopWords.contains(token)) {
						if (Character.isLetter(token.charAt(0))) {
							freqMap.put(token, freqMap.getOrDefault(token, 0) + 1);
						}
					}
				}
			}
		}
		return freqMap;
	}

	public Map<String, Integer> getFrequencyMapFromDocument(String content) throws IOException {
		Map<String, Integer> freqMap = new HashMap<>();
		if (content == null || content.isEmpty()) {
			return freqMap;
		}

		List<String> tokens = tokenize(content);
		for (String token : tokens) {
			if (token.length() > 1 && !stopWords.contains(token)) {
				if (Character.isLetter(token.charAt(0))) {
					freqMap.put(token, freqMap.getOrDefault(token, 0) + 1);
				}
			}
		}
		return freqMap;
	}

	/**
	 * Retrieves the most frequent keywords from a document string.
	 * 
	 * @param content document text / nội dung văn bản
	 * @param limit   maximum number of keywords to return / số lượng từ khóa tối đa
	 * @return ordered list of top keywords / danh sách các từ khóa hàng đầu
	 */
	public List<String> getTopKeywords(String content, int limit) {
		try {
			Map<String, Integer> freqMap = getFrequencyMapFromDocument(content);
			return freqMap.entrySet().stream()
					.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
					.limit(limit)
					.map(Map.Entry::getKey)
					.collect(Collectors.toList());
		} catch (IOException e) {
			return new ArrayList<>();
		}
	}

	// --- PHẦN 4: HÀM SO SÁNH HAI FILE ---

	/**
	 * Performs a similarity comparison between two files.
	 * 
	 * @param filePath1 path to first file / đường dẫn file thứ nhất
	 * @param filePath2 path to second file / đường dẫn file thứ hai
	 * @return comparison result including cosine and jaccard similarity / kết quả
	 *         so sánh bao gồm độ tương đồng
	 */
	public FileCompareResult compareFiles(String filePath1, String filePath2) {
		try {
			Map<String, Integer> map1 = getFrequencyMapFromFile(filePath1);
			Map<String, Integer> map2 = getFrequencyMapFromFile(filePath2);

			Set<String> commonKeywords = new HashSet<>(map1.keySet());
			commonKeywords.retainAll(map2.keySet());

			double cosineSim = calculateCosineSimilarity(map1, map2, commonKeywords);
			double jaccardSim = calculateJaccardSimilarity(map1.keySet(), map2.keySet());

			return FileCompareResult.builder()
					.commonKeywords(commonKeywords)
					.cosineSim(cosineSim)
					.jaccardSim(jaccardSim)
					.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String findMostSimilarFile(String sourceFilePath, List<String> candidateFilePaths) throws IOException {
		if (candidateFilePaths == null || candidateFilePaths.isEmpty()) {
			return null;
		}

		Map<String, Integer> sourceMap = getFrequencyMapFromFile(sourceFilePath);
		String bestFile = null;
		double bestScore = -1.0;

		for (String candidate : candidateFilePaths) {
			Map<String, Integer> candidateMap = getFrequencyMapFromFile(candidate);
			Set<String> common = new HashSet<>(sourceMap.keySet());
			common.retainAll(candidateMap.keySet());

			double cosineSim = calculateCosineSimilarity(sourceMap, candidateMap, common);
			if (cosineSim > bestScore) {
				bestScore = cosineSim;
				bestFile = candidate;
			}
		}
		return bestFile;
	}

	private double calculateCosineSimilarity(Map<String, Integer> map1, Map<String, Integer> map2,
			Set<String> intersection) {
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;

		for (String key : intersection) {
			dotProduct += map1.get(key) * map2.get(key);
		}

		for (int val : map1.values()) {
			normA += Math.pow(val, 2);
		}
		for (int val : map2.values()) {
			normB += Math.pow(val, 2);
		}

		if (normA == 0 || normB == 0) {
			return 0.0;
		}
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	private double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
		Set<String> union = new HashSet<>(set1);
		union.addAll(set2);

		Set<String> intersection = new HashSet<>(set1);
		intersection.retainAll(set2);

		if (union.isEmpty()) {
			return 0.0;
		}
		return (double) intersection.size() / union.size();
	}
}
