package com.example.demo.service.iservice;

import com.example.demo.dto.StateResponse;
import com.example.demo.dto.form.FormRequest;
import com.example.demo.dto.form.TopicRequest;

public interface IFormService {
    StateResponse<Object> getAllForm();

    StateResponse<Object> newForm(Long topicId, FormRequest formRequest);

    StateResponse<Object> getFormComment(String formId);

    StateResponse<Object> newTopic(TopicRequest request);

    StateResponse<Object> getAllTopic();

    StateResponse<Object> getAllTopics();

    StateResponse<Object> getAllFormFromTopic(Long topicId);

    StateResponse<Object> deleteForm(String formId);
}
