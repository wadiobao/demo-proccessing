package com.example.demo.modules.document.processing.infrastructure.adapter.output;

import com.example.demo.modules.document.processing.application.port.output.KeywordExtractionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class YakeKeywordExtractionAdapter implements KeywordExtractionPort {

    // private final Yake yake;

    public YakeKeywordExtractionAdapter() {
        /*
        this.yake = new Yake.Builder()
            .withMaxNGramSize(2)
            .withNumberOfKeywords(15)
            .withLanguage("vi")
            .build();
        */
        log.info("Khởi tạo YakeKeywordExtractionAdapter");
    }

    @Override
    public List<String> extractKeywords(List<String> tokens, int maxKeywords) {
        log.info("Đang trích xuất từ khóa (YAKE fallback)...");
        // Fallback: Lọc các từ dài hơn 3 ký tự và lấy N từ đầu tiên
        return tokens.stream()
                .filter(t -> t.length() > 3)
                .distinct()
                .limit(maxKeywords)
                .collect(Collectors.toList());
    }
}
