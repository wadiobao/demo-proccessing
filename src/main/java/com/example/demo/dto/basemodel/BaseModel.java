package com.example.demo.dto.basemodel;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

// @EntityListeners activates @CreatedDate / @LastModifiedDate for JPA (MySQL)
// @EnableMongoAuditing in DemoApplication activates them for MongoDB
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseModel {
	// immutable after insert — enforced at DB level for JPA
	@CreatedDate
	@Column(updatable = false)
	@JsonFormat(pattern = "dd/MM/yyyy")
	protected LocalDateTime createdAt;

	@LastModifiedDate
	@JsonFormat(pattern = "dd/MM/yyyy")
	protected LocalDateTime modifiedAt;

	@JsonFormat(pattern = "dd/MM/yyyy")
	protected LocalDateTime deletedAt;
}
