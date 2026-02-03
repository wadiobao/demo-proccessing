package com.example.demo.sql.service.iservice;

import org.springframework.data.domain.Pageable;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.dto.form.FormRequest;
import com.example.demo.sql.dto.form.TopicRequest;

public interface IFormService {
    StateResponse<Object> getAllForm(Pageable pageable);

    StateResponse<Object> newForm(Long topicId, FormRequest formRequest);

    StateResponse<Object> getFormComment(String formId, Pageable pageable);

    StateResponse<Object> newTopic(TopicRequest request);

    StateResponse<Object> getAllTopic(Pageable pageable);

    StateResponse<Object> getAllTopics(Pageable pageable);

    StateResponse<Object> getAllFormFromTopic(Long topicId, Pageable pageable);

    StateResponse<Object> deleteForm(String formId);
}
