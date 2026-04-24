package com.example.demo.modules.quiz.shared.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;

@Repository
public interface UserResourceRepository extends MongoRepository<UserResourceMongoEntity, String> {
    Optional<UserResourceMongoEntity> findByUserNameAndTopic(String userName, String topic);
    List<UserResourceMongoEntity> findAllByUserName(String userName);
}
