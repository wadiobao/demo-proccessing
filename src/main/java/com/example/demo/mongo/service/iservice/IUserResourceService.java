package com.example.demo.mongo.service.iservice;

import com.example.demo.mongo.entity.Content;

public interface IUserResourceService {
	public void save(String filename, String pdfContent, String userName, Content content);

	public boolean existsById(String id);

	public void delete(String id);
}
