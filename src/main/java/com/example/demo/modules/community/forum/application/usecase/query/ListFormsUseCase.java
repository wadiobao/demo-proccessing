package com.example.demo.modules.community.forum.application.usecase.query;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.FormResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormRepository;
import com.example.demo.modules.community.reputation.api.ReputationFacade;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListFormsUseCase {

    private final FormRepository formRepository;
    private final IUserRepository userRepository;
    private final ReputationFacade reputationFacade;

    public StateResponse<Object> execute(Pageable pageable) {
        Page<Form> all = formRepository.findAllByOrderByNgayDangDesc(pageable);
        return StateResponse.builder().result(decorateWithVotes(all)).build();
    }

    public StateResponse<Object> executeForTopic(Long topicId, String tag, Pageable pageable) {
        Page<Form> all;
        if (tag != null && !tag.isEmpty()) {
            all = formRepository.findByTopic_TopicIdAndTagsContainingOrderByNgayDangDesc(topicId, tag, pageable);
        } else {
            all = formRepository.findByTopic_TopicIdOrderByNgayDangDesc(topicId, pageable);
        }
        return StateResponse.builder().result(decorateWithVotes(all)).build();
    }

    public StateResponse<Object> searchByKeyword(String keyword, Pageable pageable) {
        Page<Form> results = formRepository.searchByKeyword(keyword, pageable);
        return StateResponse.builder().result(decorateWithVotes(results)).build();
    }

    private Page<FormResponse> decorateWithVotes(Page<Form> forms) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUserName(currentUsername).orElse(null);

        Map<String, Integer> userVotes = reputationFacade.getVotesForForms(currentUser, forms.getContent());

        return forms.map(form -> FormResponse.builder()
                .formId(form.getFormId())
                .tacGia(form.getTacGia())
                .tieuDe(form.getTieuDe())
                .tags(form.getTags())
                .ngayDang(form.getNgayDang())
                .noiDung(form.getContent().getNoiDung())
                .topic(form.getTopic().getTopic())
                .voteScore(form.getVoteScore())
                .userVoteValue(userVotes.getOrDefault(form.getFormId(), 0))
                .contentId(form.getContentId())
                .hasQuiz(form.isHasQuiz())
                .build());
    }
}
