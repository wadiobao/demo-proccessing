package com.example.demo.dto.basemodel;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@MappedSuperclass
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseModel {
	@Column(updatable = false)
	@JsonFormat(pattern = "dd/MM/yyyy")
	protected LocalDateTime createdAt;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	protected LocalDateTime modifiedAt;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	protected LocalDateTime deletedAt;
	
	@PrePersist
	public void onCreate() {
		createdAt = LocalDateTime.now();
	}
	
	@PreUpdate
	public void onUpdate() {
		modifiedAt = LocalDateTime.now();
	}
}
