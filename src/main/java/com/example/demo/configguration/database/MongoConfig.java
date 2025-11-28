package com.example.demo.configguration.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
		basePackages = {"com.example.demo.mongo.repository",
						"com.example.demo.agent.repository"
						})
public class MongoConfig {

}
