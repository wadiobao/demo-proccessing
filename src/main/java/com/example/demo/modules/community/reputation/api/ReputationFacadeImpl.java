package com.example.demo.modules.community.reputation.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.community.reputation.infrastructure.persistence.entity.Vote;
import com.example.demo.modules.community.reputation.infrastructure.persistence.repository.VoteRepository;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.infrastructure.persistence.mapper.IdentityEntityMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReputationFacadeImpl implements ReputationFacade {

    private final VoteRepository voteRepository;
    private final IdentityEntityMapper entityMapper;

    @Override
    public int getUserVote(User user, Form form) {
        if (user == null || form == null) {
			return 0;
		}
        return voteRepository.findByVoterAndTargetPost(entityMapper.toEntity(user), form)
                .map(Vote::getValue)
                .orElse(0);
    }

    @Override
    public Map<String, Integer> getVotesForForms(User user, List<Form> forms) {
        Map<String, Integer> userVotes = new HashMap<>();
        if (user != null && forms != null && !forms.isEmpty()) {
            List<Vote> votes = voteRepository.findAllByVoterAndTargetPostIn(entityMapper.toEntity(user), forms);
            for (Vote v : votes) {
                userVotes.put(v.getTargetPost().getFormId(), v.getValue());
            }
        }
        return userVotes;
    }
}
