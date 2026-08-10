package com.example.demo.modules.document.processing.application.port.output;

import java.util.List;

public interface ExtractiveSummaryPort {
    List<String> summarize(List<String> sentences, int topN);
}
