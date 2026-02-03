package com.example.demo.mongo.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.dto.basemodel.BaseModel;
import com.example.demo.mongo.dto.question.UserAnswer;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user_resource")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResource extends BaseModel{
	@Id
	String id;
	//topic is unique
	String topic;
	@Default
	List<String> contentIds = new ArrayList<String>();
	String userName;
	@Default
	List<UserAnswer> history = new ArrayList<UserAnswer>();
	@Default
	double theta = 0.0;
	@Default
	double b = 0.0;

}
