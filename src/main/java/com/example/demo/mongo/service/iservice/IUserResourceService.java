package com.example.demo.mongo.service.iservice;

import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;

public interface IUserResourceService {
	public void save(String filename, String pdfContent, String userName, DocumentMetadata content);

	public boolean existsById(String id);

	public void delete(String id);
}
