package com.example.demo.sql.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.sql.entity.FormContent;

@Repository
public interface FormContentRepository extends JpaRepository<FormContent, String> {

}
