package com.example.demo.modules.quiz.bank.api.mapper;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import com.example.demo.modules.quiz.bank.api.request.QuestionBankRequest;
import com.example.demo.modules.quiz.bank.api.response.QuestionBankResponse;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.QuestionBankMongoEntity;

/**
 * Mapper for QuestionBankMongoEntity to QuestionBankResponse and vice versa.
 */
@Mapper(componentModel = "spring")
public interface QuestionBankMapper {

    QuestionBankResponse toResponse(QuestionBankMongoEntity entity);

    QuestionBankMongoEntity toEntity(QuestionBankRequest request);

    default Page<QuestionBankResponse> toResponsePage(Page<QuestionBankMongoEntity> page) {
        return page.map(this::toResponse);
    }
}
