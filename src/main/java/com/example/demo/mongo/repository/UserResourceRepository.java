package com.example.demo.mongo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.mongo.entity.UserResource;

@Repository
public interface UserResourceRepository extends MongoRepository<UserResource, String> {
	Optional<UserResource> findByTitle(String title);
	List<String> findAllByUserName(String userId);

}
