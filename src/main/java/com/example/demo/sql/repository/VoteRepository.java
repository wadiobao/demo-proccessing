package com.example.demo.sql.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.sql.entity.Form;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.entity.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByVoterAndTargetPost(User voter, Form targetPost);

    java.util.List<Vote> findAllByVoterAndTargetPostIn(User voter, java.util.Collection<Form> posts);

    long countByTargetPostAndValue(Form targetPost, int value);
}
