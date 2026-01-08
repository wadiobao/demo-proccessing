package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.constants.Constants;
import com.example.demo.dto.StateResponse;
import com.example.demo.dto.question.FileGenerateResponse;
import com.example.demo.dto.question.Question;
import com.example.demo.mongo.entity.ArchivedQuestion;
import com.example.demo.mongo.service.ArchivedQuestionService;
import com.example.demo.mongo.service.UserResourceService;
import com.example.demo.service.iservice.IQuizService;
import com.example.demo.service.quiz.FileGenerationService;
import com.example.demo.service.quiz.GeminiAIService;
import com.example.demo.service.quiz.GeminiAIService.GeminiResponse;
import com.example.demo.service.quiz.PDFProcessingService;
import com.example.demo.utils.FileBasedKeywordExtractor;
import com.example.demo.utils.GeneralUtils;

import jakarta.transaction.Transactional;

@Service
public class QuizService implements IQuizService {

	@Value("${gemini.api.key}")
	private String geminiApiKey;

	@Value("${demo.instruction.path}")
	private String instructionPath;

	@Value("${demo.tesseract.path}")
	private String tesseractPath;

	@Value("${demo.wordfile.path}")
	private String wordPath;

	@Autowired
	private PDFProcessingService pdfProcessingService;

	@Autowired
	private GeminiAIService geminiAIService;

	@Autowired
	private FileGenerationService fileGenerationService;

	@Autowired
	private ArchivedQuestionService archivedQuestionService;
	
	@Autowired
	private UserResourceService userResourceService;
	
	@Autowired
	private FileBasedKeywordExtractor basedKeywordExtractor;
	
	@Autowired
	private GeneralUtils generalUtils;

	@Override
	@Transactional
	public StateResponse<Object> privateHandlePdf(MultipartFile file, int questionCount, int level, int type,
			String language) throws Exception {
		StateResponse<Object> response;

		if (pdfProcessingService.checkPDF(file).equals("BASE")) {
			System.out.println("BASE");
			response = handleBasePdf(file, questionCount, level, type, 0, language);
			
		} else {
			System.out.println("SCAN");
			response = handleScanPdf(file, questionCount, level, type, 0, language);
		}
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		System.out.println(authentication);
		if (authentication != null && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken)) {
			System.out.println("login");
						
			FileGenerateResponse fileGenerateResponse = (FileGenerateResponse) response.getResult();
			
			ArchivedQuestion pdfStore = ArchivedQuestion.builder()
					.author(authentication.getName())
					.content(fileGenerateResponse.getQuestions())
					.pdfBase64(fileGenerateResponse.getPdfBase64())
					.wordBase64(fileGenerateResponse.getWordBase64())
					.title(file.getOriginalFilename()).build();
			archivedQuestionService.save(pdfStore);
			
			
			String fileName = file.getOriginalFilename();
			String pdfContent = fileGenerateResponse.getContentPdf();
			String username = authentication.getName();
			String id = generalUtils.sha256(pdfContent+username);
			
			if(!userResourceService.existsById(id)) {
				userResourceService.save(id,fileName, pdfContent, username);
			}

		}
		return response;
	}

	@Override
	public StateResponse<Object> publicHandlePdf(MultipartFile file, int questionCount, int level, int type,
			String language) throws Exception {
		StateResponse<Object> response;
		if (pdfProcessingService.checkPDF(file).equals("BASE")) {
			System.out.println("BASE");
			response = handleBasePdf(file, questionCount, level, type, 0, language);
		} else {
			System.out.println("SCAN");
			response = handleScanPdf(file, questionCount, level, type, 0, language);
		}
		return response;
	}

	@Override
	public StateResponse<Object> handleBasePdf(MultipartFile file, int questionCount, int level, int type, int imgQuest,
			String language) {
		try {
			if (!file.getContentType().equals(Constants.FileTypes.PDF)) {
				throw new IllegalArgumentException(Constants.Messages.PDF_ONLY);
			}
			String pdfText = pdfProcessingService.extractTextFromPdf(file);
			
			String prompt = null;
			GeminiResponse geminiResponse = null;
			
			if(level == 2) {
				//prompt = combineReGenPrompt(questionCount, level, language,pdfText);
				geminiResponse = geminiAIService.reGenerateQuestionWithGemini(prompt);
			}else {
				prompt = combinePrompt(questionCount, level, pdfText, type, imgQuest, language);
				geminiResponse = geminiAIService.generateQuestionWithGemini(prompt);
			}
			
			System.out.println(prompt.substring(0, 70));
			
			String[] wordAndPdf = fileGenerationService.generateWordAndPdfBase64(geminiResponse.getQuestions());
			if (wordAndPdf == null) {
				return StateResponse.builder().message(Constants.Messages.FILE_GENERATION_ERROR).build();
			}

			List<Question> questions = geminiResponse.getQuestions();

			if (imgQuest == 1) {
				for (Question question : questions) {
					if (question.getImgPrompt() != null) {
						String imgAttribute[] = geminiAIService.generateImageWithGemini(question.getImgPrompt(),
								question.getId());
						String imgUrl = imgAttribute[1];
						String imgPublicId = imgAttribute[0];
						question.setImgPublicId(imgPublicId);
						question.setImgUrl(imgUrl);
					}
				}
			}

			FileGenerateResponse response = FileGenerateResponse.builder().questions(questions)
					.wordBase64(wordAndPdf[0]).pdfBase64(wordAndPdf[1])
					.contentPdf(pdfText).build();

			return StateResponse.builder().result(response).build();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return StateResponse.builder().message(Constants.Messages.FILE_GENERATION_ERROR).build();
		}
	}

	@Override
	public StateResponse<Object> handleScanPdf(MultipartFile file, int questionCount, int level, int type, int imgQuest,
			String language) {
		try {
			if (!file.getContentType().equals(Constants.FileTypes.PDF)) {
				throw new IllegalArgumentException(Constants.Messages.PDF_ONLY);
			}

			String pdfText = pdfProcessingService.renderPdfToPngToString(file);
			String prompt = combinePrompt(questionCount, level, pdfText, type, imgQuest, language);
			GeminiResponse geminiResponse = geminiAIService.generateQuestionWithGemini(prompt);

			String[] wordAndPdf = fileGenerationService.generateWordAndPdfBase64(geminiResponse.getQuestions());
			if (wordAndPdf == null) {
				return StateResponse.builder().message(Constants.Messages.FILE_GENERATION_ERROR).build();
			}

			List<Question> questions = geminiResponse.getQuestions();

			if (imgQuest == 1) {
				for (Question question : questions) {
					if (question.getImgPrompt() != null) {
						String imgAttribute[] = geminiAIService.generateImageWithGemini(question.getImgPrompt(),
								question.getId());
						String imgUrl = imgAttribute[1];
						String imgPublicId = imgAttribute[0];
						question.setImgPublicId(imgPublicId);
						question.setImgUrl(imgUrl);
					}
				}
			}

			FileGenerateResponse response = FileGenerateResponse.builder().questions(geminiResponse.getQuestions())
					.wordBase64(wordAndPdf[0]).pdfBase64(wordAndPdf[1])
					.contentPdf(pdfText).build();

			return StateResponse.builder().result(response).build();
		} catch (Exception e) {
			return StateResponse.builder().message(Constants.Messages.FILE_GENERATION_ERROR).build();
		}
	}

	@Override
	public String combinePrompt(int questionCount, int level, String pdfText, int type, int imgQuesion,
			String language) {
		return String.format(
				Constants.QuestionFormat.QUESTION_COUNT 
				+ Constants.QuestionFormat.DIFFICULTY_LEVEL
				+ Constants.QuestionFormat.KNOWLEDGE_TYPE 
				+ Constants.QuestionFormat.IMAGE_PRESENTATION
				+ Constants.QuestionFormat.LANGUAGE
				+ Constants.QuestionFormat.DOCUMENT_PROVIDED,
				questionCount, level, type, imgQuesion,language, pdfText);
	}
	
	public String combineReGenPrompt(int questionCount, double min, double max,String language, String pdfText) {
		return String.format(
				Constants.QuestionFormat.QUESTION_COUNT 
				+ Constants.QuestionFormat.MIN_DIFFICULT
				+ Constants.QuestionFormat.MAX_DIFFICULT
				+ Constants.QuestionFormat.LANGUAGE
				+ Constants.QuestionFormat.DOCUMENT_PROVIDED,
				questionCount, min,max,language,pdfText);
	}
	
	
	
	
	public void getB() {
		
	}

}
