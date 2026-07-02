package com.example.demo.modules.community.forum.infrastructure.persistence.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "topic_data")
public class Topic {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	Long topicId;
	String topic;

	// Cascade ALL + orphanRemoval ensures Form (and its Comment children) are deleted when Topic is deleted
	@OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	List<Form> forms;

}
