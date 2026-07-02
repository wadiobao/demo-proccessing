package com.example.demo.modules.community.forum.application.usecase.command;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Topic;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.CommentRepository;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.TopicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDeleteTopicUseCase {

    private final TopicRepository topicRepository;
    private final CommentRepository commentRepository;

    /**
     * Delete a Topic and cascade delete all associated Forms and Comments.
     * Only users with ADMIN role are authorized.
     * 
     * @param topicId ID of the topic to delete
     * @return StateResponse indicating successful deletion
     * @throws RuntimeException if the topic is not found
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public StateResponse<Object> execute(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        // Explicitly clean up comments for all forms in this topic to avoid constraint violations
        List<Form> forms = topic.getForms();
        if (forms != null) {
            for (Form form : forms) {
                commentRepository.deleteAllByFormId(form.getFormId());
            }
        }

        // Deleting topic will cascade delete Forms and their FormContents due to CascadeType.ALL
        topicRepository.delete(topic);

        return StateResponse.builder().message("Topic and all its forms and comments deleted successfully").build();
    }
}
