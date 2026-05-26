package com.example.demo.modules.community.forum.infrastructure.persistence.entity;

import java.util.Date;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
@Table(name = "form_data")
public class Form {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	String formId;

	String tacGia;
	String tieuDe;

	@Builder.Default
	int voteScore = 0; // Read-optimized counter

	String contentId; // Reference to MongoDB Content/Quiz
	boolean hasQuiz; // Indicator for UI

	@ElementCollection(fetch = FetchType.LAZY)
	Set<String> tags;

	@OneToOne(mappedBy = "form", fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
	FormContent content;

	@Temporal(TemporalType.TIMESTAMP)
	Date ngayDang;

	@ManyToOne
	@JoinColumn(name = "topic_id")
	Topic topic;

	@jakarta.persistence.Column(columnDefinition = "boolean default false")
	Boolean hasChanged = false;
}
