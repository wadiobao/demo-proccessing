package com.example.demo.sql.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.sql.entity.Form;
import com.example.demo.sql.entity.NormalUser;
import com.example.demo.sql.entity.Tier;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.entity.Vote;
import com.example.demo.sql.repository.TierRepository;
import com.example.demo.sql.repository.UserRepository;
import com.example.demo.sql.repository.VoteRepository;
import com.example.demo.mongo.service.QuestionBankService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing user reputation and prestige-based roles.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReputationService {

    UserRepository userRepository;
    VoteRepository voteRepository;
    TierRepository tierRepository;
    QuestionBankService questionBankService;

    private static final int UPVOTE_VALUE = 5;
    private static final int DOWNVOTE_VALUE = -2;

    /**
     * Records a vote from one user to another's post and updates reputation.
     * 
     * @param voter user casting the vote
     * @param post  the post being voted on
     * @param value 1 for up, -1 for down
     */
    @Transactional
    public void castVote(User voter, Form post, int value) {
        if (value != 1 && value != -1 && value != 0) {
            throw new IllegalArgumentException("Invalid vote value. Allowed: 1 (Up), -1 (Down), 0 (None)");
        }

        // 1. Handle Idempotency (Update if exists, or create new)
        Vote vote = voteRepository.findByVoterAndTargetPost(voter, post)
                .orElse(Vote.builder().voter(voter).targetPost(post).build());

        int oldValue = vote.getId() != null ? vote.getValue() : 0;
        if (oldValue == value) {
            return; // No change
        }

        vote.setValue(value);
        voteRepository.save(vote);

        // 2. Update Form Aggregate Counter (Total Vote Score)
        int deltaVote = value - oldValue;
        post.setVoteScore(post.getVoteScore() + deltaVote);
        // Form is managed here so it will be saved if repository is called or
        // transaction ends

        // 3. Resolve Author (tacGia is a string in Form, need to find User)
        String authorName = post.getTacGia();
        userRepository.findByUserName(authorName).ifPresent(author -> {
            // Re-calculate author reputation based on delta
            int deltaRep = calculateReputationDelta(oldValue, value);
            updateUserReputation(author, deltaRep);
        });

        // 4. Governance Threshold Check: If score > 100, promote linked quiz
        if (post.getVoteScore() > 100 && post.getContentId() != null) {
            questionBankService.promoteByContentId(post.getContentId());
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

            // Dynamic Tier Promotion/Demotion
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

    /**
     * Resets negative reputations to 0 monthly.
     */
    @Transactional
    public void performMonthlyReset() {
        log.info("Starting monthly reputation reset...");
        Tier contributorTier = tierRepository.findById("CONTRIBUTOR").orElse(null);
        
        userRepository.findAll().forEach(user -> {
            if (user instanceof NormalUser normalUser && normalUser.getReputationScore() < 0) {
                normalUser.setReputationScore(0);
                normalUser.setCurrentTier(contributorTier);
                normalUser.setLastReputationReset(LocalDateTime.now());
                userRepository.save(normalUser);
            }
        });
    }
}
