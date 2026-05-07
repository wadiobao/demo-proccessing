package com.example.demo.modules.community.forum.application.usecase.query;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.FormResponse;
import com.example.demo.modules.community.forum.api.dto.TopicResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Topic;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormRepository;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.TopicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListTopicsUseCase {

    private final TopicRepository topicRepository;
    private final FormRepository formRepository;

    public StateResponse<Object> execute(Pageable pageable) {
        Page<Topic> topics = topicRepository.findAll(pageable);
        Page<TopicResponse> responses = topics.map(topic -> {
            List<Form> forms = formRepository.findByTopic_TopicIdOrderByNgayDangDesc(topic.getTopicId(), Pageable.unpaged()).getContent();
            List<FormResponse> formResponses = new ArrayList<>();
            for (Form form : forms) {
                FormResponse formResponse = FormResponse.builder()
                        .formId(form.getFormId())
                        .tacGia(form.getTacGia())
                        .tieuDe(form.getTieuDe())
                        .tags(form.getTags())
                        .ngayDang(form.getNgayDang())
                        .noiDung(form.getContent().getNoiDung())
                        .topic(topic.getTopic())
                        .hasQuiz(form.isHasQuiz())
                        .contentId(form.getContentId())
                        .build();
                formResponses.add(formResponse);
            }
            return TopicResponse.builder().topicId(topic.getTopicId()).topic(topic.getTopic()).forms(formResponses)
                    .build();
        });
        return StateResponse.builder().result(responses).build();
    }

    public StateResponse<Object> executeWithoutForms(Pageable pageable) {
        Page<Topic> topics = topicRepository.findAll(pageable);
        Page<TopicResponse> responses = topics
                .map(topic -> TopicResponse.builder().topicId(topic.getTopicId()).topic(topic.getTopic()).build());
        return StateResponse.builder().result(responses).build();
    }
}
