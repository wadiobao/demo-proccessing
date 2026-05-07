package com.example.demo.modules.community.reputation.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.identity.infrastructure.persistence.entity.UserEntity;
import com.example.demo.modules.community.reputation.infrastructure.persistence.entity.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByVoterAndTargetPost(UserEntity voter, Form targetPost);

    java.util.List<Vote> findAllByVoterAndTargetPostIn(UserEntity voter, java.util.Collection<Form> posts);

    long countByTargetPostAndValue(Form targetPost, int value);
}
