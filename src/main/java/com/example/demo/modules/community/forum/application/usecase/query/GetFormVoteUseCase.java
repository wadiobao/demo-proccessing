package com.example.demo.modules.community.forum.application.usecase.query;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.VoteResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormRepository;
import com.example.demo.modules.community.reputation.api.ReputationFacade;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFormVoteUseCase {

    private final FormRepository formRepository;
    private final IUserRepository userRepository;
    private final ReputationFacade reputationFacade;

    @Transactional(readOnly = true)
    public StateResponse<Object> execute(String formId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUserName(currentUsername).orElse(null);

        int userVoteValue = reputationFacade.getUserVote(currentUser, form);

        return StateResponse.builder()
                .result(VoteResponse.builder()
                        .voteScore(form.getVoteScore())
                        .userVoteValue(userVoteValue)
                        .build())
                .build();
    }
}
