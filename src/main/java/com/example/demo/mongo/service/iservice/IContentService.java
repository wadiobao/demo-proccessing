package com.example.demo.mongo.service.iservice;

import java.io.IOException;
import java.util.List;

import com.example.demo.mongo.entity.Content;

public interface IContentService {
	public Content save(String content, String owner) throws IOException;
	public Content searchSimilar(List<Double> queryVector, int limit, String username);
}
