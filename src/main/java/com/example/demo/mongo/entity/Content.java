package com.example.demo.mongo.entity;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.dto.basemodel.BaseModel;

import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@Document(collection = "content")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Content extends BaseModel {
	@Id
	String id;
	String content;
	String owner;
	List<String> tags;
	List<Double> embedding;
	String topic;
	@Transient
	Double vectorSearchScore;
}
