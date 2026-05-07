package com.example.demo.modules.community.reputation.application.usecase.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.community.reputation.infrastructure.persistence.entity.Vote;
import com.example.demo.modules.community.reputation.infrastructure.persistence.repository.VoteRepository;
import com.example.demo.modules.identity.domain.model.NormalUser;
import com.example.demo.modules.identity.domain.model.Tier;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.ITierRepository;
import com.example.demo.modules.identity.domain.repository.IUserRepository;
import com.example.demo.modules.identity.infrastructure.persistence.mapper.IdentityEntityMapper;
import com.example.demo.modules.quiz.bank.application.usecase.PromoteBankQuestionsUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CastVoteUseCase {

    private final IUserRepository userRepository;
    private final VoteRepository voteRepository;
    private final ITierRepository tierRepository;
    private final PromoteBankQuestionsUseCase promoteBankQuestionsUseCase;
    private final IdentityEntityMapper identityMapper;

    private static final int UPVOTE_VALUE = 5;
    private static final int DOWNVOTE_VALUE = -2;

    @Transactional
    public void execute(User voter, Form post, int value) {
        if (value != 1 && value != -1 && value != 0) {
            throw new IllegalArgumentException("Invalid vote value. Allowed: 1 (Up), -1 (Down), 0 (None)");
        }

        Vote vote = voteRepository.findByVoterAndTargetPost(identityMapper.toEntity(voter), post)
                .orElse(Vote.builder().voter(identityMapper.toEntity(voter)).targetPost(post).build());

        int oldValue = vote.getId() != null ? vote.getValue() : 0;
        if (oldValue == value) {
            return;
        }

        vote.setValue(value);
        voteRepository.save(vote);

        int deltaVote = value - oldValue;
        post.setVoteScore(post.getVoteScore() + deltaVote);

        String authorName = post.getTacGia();
        userRepository.findByUserName(authorName).ifPresent(author -> {
            int deltaRep = calculateReputationDelta(oldValue, value);
            updateUserReputation(author, deltaRep);
        });

        if (post.getVoteScore() > 100 && post.getContentId() != null) {
            promoteBankQuestionsUseCase.execute(post.getContentId());
        }
    }

    private int calculateReputationDelta(int oldVal, int newVal) {
        int oldRep = (oldVal == 1) ? UPVOTE_VALUE : (oldVal == -1 ? DOWNVOTE_VALUE : 0);
        int newRep = (newVal == 1) ? UPVOTE_VALUE : (newVal == -1 ? DOWNVOTE_VALUE : 0);
        return newRep - oldRep;
    }

    private void updateUserReputation(User user, int delta) {
        if (user instanceof NormalUser normalUser) {
            int newScore = normalUser.getReputationScore() + delta;
            normalUser.setReputationScore(newScore);

            Tier newTier = determineTier(newScore);
            Tier currentTier = normalUser.getCurrentTier();
            
            if (currentTier == null || !newTier.getId().equals(currentTier.getId())) {
                log.info("User {} changed tier: {} -> {}", normalUser.getUserName(), 
                    currentTier != null ? currentTier.getId() : "NONE", newTier.getId());
                normalUser.setCurrentTier(newTier);
            }
            userRepository.save(normalUser);
        }
    }

    private Tier determineTier(int score) {
        return tierRepository.findAll().stream()
                .filter(t -> score >= t.getMinReputation())
                .sorted((t1, t2) -> Integer.compare(t2.getMinReputation(), t1.getMinReputation()))
                .findFirst()
                .orElseGet(() -> tierRepository.findById("CONTRIBUTOR").orElse(null));
    }
}
