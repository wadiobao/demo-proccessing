package com.example.demo.mongo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.mapper.PdfStoreResponse;
import com.example.demo.mongo.dto.question.Question;
import com.example.demo.mongo.entity.ArchivedQuestion;
import com.example.demo.mongo.repository.ArchivedQuestionRepository;
import com.example.demo.mongo.service.iservice.IArchivedQuestionService;
import com.example.demo.utils.CloudinaryUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArchivedQuestionService implements IArchivedQuestionService {
	ArchivedQuestionRepository archivedQuestionRepository;
	
	CloudinaryUtils cloudinaryUtils;
	
	@Override
	public ArchivedQuestion save(ArchivedQuestion pdfStore) throws Exception {
		if(archivedQuestionRepository.countByAuthor(pdfStore.getAuthor())>=6) {
			delete(pdfStore.getAuthor());
		}
		pdfStore.setCreatedAt(LocalDateTime.now());
		return archivedQuestionRepository.save(pdfStore);
	}
	
	@Override
	public StateResponse<Object> findByAuthor(String author) {
		List<ArchivedQuestion> authorPdfs = archivedQuestionRepository.findAllByAuthorOrderByCreatedAtDesc(author);
		List<PdfStoreResponse> responses = new ArrayList<PdfStoreResponse>();
		for (ArchivedQuestion pdfStore : authorPdfs) {
			responses.add(PdfStoreResponse.builder()
					.author(author)
					.title(pdfStore.getTitle())
					.questions(pdfStore.getQuestions())
					.createdAt(pdfStore.getCreatedAt())
					.pdfBase64(pdfStore.getPdfBase64())
					.wordBase64(pdfStore.getWordBase64())
					.build());
		}
		return StateResponse.builder().result(responses).build();
	}
	
	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public List<ArchivedQuestion> findAll() {
		List<ArchivedQuestion> authorPdfs = archivedQuestionRepository.findAll();
		return authorPdfs;
	}
	
	@Override
	public void delete(String author) throws Exception {
		ArchivedQuestion pdfStore = archivedQuestionRepository.findFirstByAuthorOrderByCreatedAtAsc(author).orElseThrow(()-> new HandleException(ErrorCode.USER_NOT_EXISTED));
		List<String> deleteImgList = new ArrayList<String>();
		List<Question> questions = pdfStore.getQuestions();
		for (Question question : questions) {
			if(question.getImgPublicId()!=null) {
				deleteImgList.add(question.getImgPublicId());
			}
		}
		if(!deleteImgList.isEmpty()) {
			cloudinaryUtils.delete(deleteImgList);
			System.out.println("đã xóa ảnh id"+ deleteImgList.toString());
		}
		archivedQuestionRepository.deleteById(pdfStore.getId());
	}

	@Override
	public ArchivedQuestion findByAuthorAndTitle(String author, String title) {
		return archivedQuestionRepository.findByAuthorAndTitle(author, title);
	}

	@Override
	public boolean isEvaluated(String id) {
		ArchivedQuestion archivedQuestion = archivedQuestionRepository.findById(id)
				.orElseThrow(() ->  new HandleException(ErrorCode.RESOURCE_NOT_FOUND));
		return archivedQuestion.isEvaluated();
	}
	
	
	
}
