package com.example.demo.mongo.service.iservice;

import java.util.List;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.entity.ArchivedQuestion;

public interface IArchivedQuestionService {
	public ArchivedQuestion save(ArchivedQuestion pdfStore) throws Exception;
	public StateResponse<Object> findByAuthor(String author);
	public List<ArchivedQuestion> findAll();
	public void delete(String author) throws Exception;
	ArchivedQuestion findByAuthorAndTitle(String author, String title);
	boolean isEvaluated(String id);
}
