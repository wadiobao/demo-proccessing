package com.example.demo.sql.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.sql.entity.TagRelation;

@Repository
public interface TagRelationRepository extends JpaRepository<TagRelation, Long> {
    
    @Query("SELECT t FROM TagRelation t WHERE (t.tag1Name = :tag1 AND t.tag2Name = :tag2) OR (t.tag1Name = :tag2 AND t.tag2Name = :tag1)")
    Optional<TagRelation> findByTagsPair(@Param("tag1") String tag1, @Param("tag2") String tag2);

    /**
     * Finds related tags for a given set of input tags, ordering by weight descending.
     * Use Pageable to limit the result (LIMIT K).
     */
    @Query("SELECT CASE WHEN t.tag1Name IN :inputTags THEN t.tag2Name ELSE t.tag1Name END " +
           "FROM TagRelation t " +
           "WHERE (t.tag1Name IN :inputTags OR t.tag2Name IN :inputTags) " +
           "AND NOT (t.tag1Name IN :inputTags AND t.tag2Name IN :inputTags) " +
           "ORDER BY t.weightScore DESC")
    List<String> findMostRelatedTagsExcludingInput(@Param("inputTags") List<String> inputTags, Pageable pageable);
}
