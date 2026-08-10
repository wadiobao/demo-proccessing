package com.example.demo.modules.quiz.graph.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.quiz.graph.infrastructure.persistence.entity.Tag;



@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findByName(String name);
    
    java.util.List<Tag> findByNameIn(java.util.List<String> names);    
}
