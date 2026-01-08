package com.example.demo.mongo.service.iservice;

import com.example.demo.dto.StateResponse;

public interface IUserResourceService {
	public void save(String id,String filename,String pdfContent,String userName);
	public boolean existsById(String id);
	public StateResponse<Object> findByTitle(String author);
	public void delete(String id);
}
