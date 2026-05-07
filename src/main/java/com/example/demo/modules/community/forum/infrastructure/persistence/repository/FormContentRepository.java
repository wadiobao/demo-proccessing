package com.example.demo.modules.community.forum.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.community.forum.infrastructure.persistence.entity.FormContent;

@Repository
public interface FormContentRepository extends JpaRepository<FormContent, String> {

}
