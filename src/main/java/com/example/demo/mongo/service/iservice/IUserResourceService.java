package com.example.demo.mongo.service.iservice;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.entity.UserResource;

public interface IUserResourceService {
	public UserResource save(UserResource resource);
	public StateResponse<Object> findByTitle(String author);
	public void delete(String id);
}
