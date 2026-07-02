package com.example.demo.modules.community.forum.application.usecase.query;

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
public class AdminGetAllFormsUseCase {

    private final FormRepository formRepository;
    private final IUserRepository userRepository;
    private final ReputationFacade reputationFacade;

    /**
     * Retrieve all forms with optional author filtering, mapped and decorated for admin view.
     * 
     * @param tacGia optional author name to filter by
     * @param pageable pagination details
     * @return StateResponse containing the paginated decorated form responses
     */
    public StateResponse<Object> execute(String tacGia, Pageable pageable) {
        Page<Form> forms;
        if (tacGia != null && !tacGia.trim().isEmpty()) {
            forms = formRepository.findByTacGiaOrderByNgayDangDesc(tacGia, pageable);
        } else {
            forms = formRepository.findAllByOrderByNgayDangDesc(pageable);
        }

        Page<FormResponse> responses = decorateWithVotes(forms);
        return StateResponse.builder().result(responses).build();
    }

    /**
     * Decorate forms with vote statistics and ownership information.
     */
    private Page<FormResponse> decorateWithVotes(Page<Form> forms) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUserName(currentUsername).orElse(null);

        Map<String, Integer> userVotes = reputationFacade.getVotesForForms(currentUser, forms.getContent());

        return forms.map(form -> {
            boolean isFormAuthor = currentUser != null && form.getTacGia().equals(currentUser.getUserName());
            return FormResponse.builder()
                    .formId(form.getFormId())
                    .tacGia(form.getTacGia())
                    .tieuDe(form.getTieuDe())
                    .tags(form.getTags())
                    .ngayDang(form.getNgayDang())
                    .noiDung(form.getContent().getNoiDung())
                    .topic(form.getTopic() != null ? form.getTopic().getTopic() : null)
                    .voteScore(form.getVoteScore())
                    .userVoteValue(userVotes.getOrDefault(form.getFormId(), 0))
                    .contentId(form.getContentId())
                    .hasQuiz(form.isHasQuiz())
                    .hasChanged(form.getHasChanged())
                    .isAuthor(isFormAuthor)
                    .build();
        });
    }
}
