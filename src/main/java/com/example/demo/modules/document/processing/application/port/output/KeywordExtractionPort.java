package com.example.demo.modules.document.processing.application.port.output;

import java.util.List;

public interface KeywordExtractionPort {
    List<String> extractKeywords(List<String> tokens, int maxKeywords);
}
