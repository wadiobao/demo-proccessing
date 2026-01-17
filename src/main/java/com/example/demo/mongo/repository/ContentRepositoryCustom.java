package com.example.demo.mongo.repository;

import java.util.List;

import com.example.demo.mongo.entity.Content;

public interface ContentRepositoryCustom {
	List<Content> searchSimilar(List<Double> queryVector, int limit, String username);
}
