package com.example.demo.modules.document.processing.infrastructure.adapter.output;

import com.example.demo.modules.document.processing.application.port.output.ExtractiveSummaryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class TextRankSummaryAdapter implements ExtractiveSummaryPort {

    public TextRankSummaryAdapter() {
        log.info("Khởi tạo TextRankSummaryAdapter");
    }

    @Override
    public List<String> summarize(List<String> sentences, int topN) {
        log.info("Đang tóm tắt trích xuất (TextRank fallback)...");
        // Fallback: Trả về N câu đầu tiên của văn bản
        return sentences.stream()
                .limit(topN)
                .toList();
    }
}
