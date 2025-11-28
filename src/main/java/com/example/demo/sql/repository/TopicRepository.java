package com.example.demo.sql.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.sql.entity.Topic;

@Repository
public interface TopicRepository extends JpaRepository<Topic,Long> {

}
