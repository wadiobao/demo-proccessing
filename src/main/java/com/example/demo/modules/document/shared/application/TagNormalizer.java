package com.example.demo.modules.document.shared.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Normalizes tag strings for consistent storage and graph indexing.
 * Handles Vietnamese Unicode, special characters, and known synonyms.
 */
@Component
@ConfigurationProperties(prefix = "app.tags")
public class TagNormalizer {

    private Map<String, String> synonyms = new HashMap<>();

    public Map<String, String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Map<String, String> synonyms) {
        this.synonyms = synonyms;
    }

    /**
     * Normalize a single tag: NFC normalization, lowercase, strip special chars,
     * collapse whitespace to hyphens, apply synonym map.
     */
    public String normalize(String tag) {
        if (tag == null || tag.isBlank()) return "";
        // 1. NFC normalization first - mandatory for Vietnamese
        String result = java.text.Normalizer.normalize(tag.trim(), java.text.Normalizer.Form.NFC);
        // 2. Lowercase
        result = result.toLowerCase();
        // 3. Replace spaces with hyphens
        result = result.replaceAll("\\s+", "-");
        // 4. Strip special characters
        result = result.replaceAll("[^\\p{L}\\p{N}\\-/]", "");
        // 5. Synonym mapping
        return synonyms.getOrDefault(result, result);
    }

    /**
     * Normalize a list of tags, removing blanks and duplicates.
     *
     * @param tags raw tag list from AI or metadata
     * @return deduplicated, normalized tag list
     */
    public List<String> normalizeAll(List<String> tags) {
        if (tags == null || tags.isEmpty()) return List.of();
        return tags.stream()
                .map(this::normalize)
                .filter(t -> !t.isBlank() && t.length() > 1)
                .distinct()
                .toList();
    }
}
