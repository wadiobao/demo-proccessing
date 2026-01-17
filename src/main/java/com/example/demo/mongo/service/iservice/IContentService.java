package com.example.demo.mongo.service.iservice;

import java.util.List;

import com.example.demo.mongo.entity.Content;

public interface IContentService {
	public Content save(String content, String owner);
	public Content searchSimilar(List<Double> queryVector, int limit, String username);
}
