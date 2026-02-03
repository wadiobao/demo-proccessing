package com.example.demo.mongo.service.iservice;

public interface IUserResourceService {
	public void save(String filename, String pdfContent, String userName);

	public boolean existsById(String id);

	public void delete(String id);
}
