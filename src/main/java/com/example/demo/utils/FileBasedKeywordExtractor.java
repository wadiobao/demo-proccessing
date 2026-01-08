package com.example.demo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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

import org.springframework.stereotype.Component;

import com.example.demo.dto.quiz.FileCompareResult;

@Component
public class FileBasedKeywordExtractor {

	private Set<String> dictionary;
	private Set<String> stopWords;
	private int maxSyllables = 1;
	private static final Pattern TEXT_PATTERN = Pattern.compile("\"text\":\\s*\"([^\"]+)\"");

	public FileBasedKeywordExtractor() {
		this.dictionary = new HashSet<>();
		this.stopWords = new HashSet<>();
	}

	// --- PHẦN 1: NẠP TỪ ĐIỂN (Giữ nguyên) ---
	public void loadDictionary(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				Matcher matcher = TEXT_PATTERN.matcher(line); // Đọc format JSON {"text": "..."}
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

	public void loadStopWords(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				String word = line.trim().toLowerCase();
				if (!word.isEmpty()) {
					stopWords.add(word);
				}
			}
		}
	}

	// --- PHẦN 2: XỬ LÝ TÁCH TỪ (Giữ nguyên) ---
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

	/**
	 * Đọc file văn bản từng dòng và trả về Map<Keyword, Frequency> Cách này tối ưu
	 * RAM vì không load cả nội dung file vào biến String.
	 */
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
				// Tách từ cho từng dòng
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

	// --- PHẦN 4: HÀM SO SÁNH HAI FILE (MỚI) ---

	public FileCompareResult compareFiles(String filePath1, String filePath2) {
		try {
			System.out.println("Đang phân tích File 1...");
			Map<String, Integer> map1 = getFrequencyMapFromFile(filePath1);

			map1.entrySet().stream().sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())).limit(50)
					.forEach(e -> System.out.printf("- %s (F1: %d)\n", e.getKey(), e.getValue()));

			System.out.println("Đang phân tích File 2...");
			Map<String, Integer> map2 = getFrequencyMapFromFile(filePath2);

			// 1. Tìm từ khóa chung
			Set<String> commonKeywords = new HashSet<>(map1.keySet());
			commonKeywords.retainAll(map2.keySet()); // Phép giao (Intersection)

			// 2. Tính Cosine Similarity (Độ tương đồng dựa trên vector)
			double cosineSim = calculateCosineSimilarity(map1, map2, commonKeywords);

			// 3. Tính Jaccard Similarity (Độ tương đồng dựa trên tập hợp)
			double jaccardSim = calculateJaccardSimilarity(map1.keySet(), map2.keySet());

			// --- XUẤT BÁO CÁO ---
			System.out.println("\n================ BÁO CÁO SO SÁNH ================");
			System.out.printf("File 1: %d từ khóa | File 2: %d từ khóa\n", map1.size(), map2.size());
			System.out.println("-------------------------------------------------");
			System.out.printf("Độ tương đồng (Cosine):  %.2f%% (Dựa trên tần suất)\n", cosineSim * 100);
			System.out.printf("Độ tương đồng (Jaccard): %.2f%% (Dựa trên sự xuất hiện)\n", jaccardSim * 100);
			System.out.println("-------------------------------------------------");
			System.out.println("SỐ TỪ KHÓA CHUNG: " + commonKeywords.size());

			// In top 10 từ khóa chung
			System.out.println("\nTop 10 từ khóa chung nổi bật nhất:");
			commonKeywords.stream()
					.sorted((k1, k2) -> Integer.compare(map1.get(k2) + map2.get(k2), map1.get(k1) + map2.get(k1)))
					.limit(10).forEach(k -> System.out.printf("- %s (F1: %d, F2: %d)\n", k, map1.get(k), map2.get(k)));

			FileCompareResult result = FileCompareResult.builder().commonKeywords(commonKeywords).cosineSim(cosineSim)
					.jaccardSim(jaccardSim).build();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Tìm tài liệu có độ tương đồng cao nhất (dựa trên Cosine) so với file đầu vào.
	 * 
	 * @param sourceFilePath     file cần so sánh
	 * @param candidateFilePaths danh sách các file ứng viên
	 * @return path của file ứng viên có điểm Cosine cao nhất, null nếu danh sách rỗng
	 * @throws IOException khi đọc file thất bại
	 */
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

	// Công thức Cosine Similarity: (A . B) / (||A|| * ||B||)
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

	// Công thức Jaccard: (A giao B) / (A hợp B)
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

	// --- MAIN ---
	public static void main(String[] args) throws IOException {
		FileBasedKeywordExtractor comparator = new FileBasedKeywordExtractor();

		long a = System.currentTimeMillis();

		for (int i = 0; i < 1; i++) {
			// 1. Cấu hình
			comparator.loadDictionary(
					"C:\\Users\\Wdibao\\Main\\Downloads\\demo\\src\\main\\resources\\instructions\\words.txt");
			comparator.loadStopWords(
					"C:\\Users\\Wdibao\\Main\\Downloads\\demo\\src\\main\\resources\\instructions\\vietnamese-stopwords.txt");

			// 2. Tạo file giả lập để test (Xóa khi chạy thật)
			// createDummyFile("doc1.txt", "Trí tuệ nhân tạo đang phát triển. Máy tính học
			// dữ liệu lớn.");
			// createDummyFile("doc2.txt", "Trí tuệ nhân tạo cần dữ liệu lớn. Máy chủ xử lý
			// thuật toán.");

			// 3. So sánh
			comparator.compareFiles(
					"C:\\Users\\Wdibao\\Main\\Downloads\\demo\\src\\main\\resources\\instructions\\test.txt",
					"C:\\Users\\Wdibao\\Main\\Downloads\\demo\\src\\main\\resources\\instructions\\dog2.txt");
		}
		long b = System.currentTimeMillis();

		System.out.println(b - a);
	}

	private static void createDummyFile(String path, String content) throws IOException {
		try (FileWriter w = new FileWriter(path)) {
			w.write(content);
		}
	}
}
