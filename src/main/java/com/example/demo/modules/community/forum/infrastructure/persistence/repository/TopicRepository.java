package com.example.demo.modules.community.forum.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Topic;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    
    /**
     * Retrieve all topics ordered by TopicId in ascending order for administration overview.
     */
    Page<Topic> findAllByOrderByTopicIdAsc(Pageable pageable);
}

