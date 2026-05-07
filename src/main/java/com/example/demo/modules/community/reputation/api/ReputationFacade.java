package com.example.demo.modules.community.reputation.api;

import java.util.List;
import java.util.Map;

import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.identity.domain.model.User;

/**
 * Public Facade for the Reputation & Gamification module.
 */
public interface ReputationFacade {

    /**
     * Gets the vote value (-1, 0, 1) a specific user cast on a specific form.
     */
    int getUserVote(User user, Form form);

    /**
     * Retrieves the vote values for a user across multiple forms.
     * @return Map where key is FormId and value is VoteValue
     */
    Map<String, Integer> getVotesForForms(User user, List<Form> forms);
}
