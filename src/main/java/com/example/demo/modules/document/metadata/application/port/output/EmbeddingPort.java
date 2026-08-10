package com.example.demo.modules.document.metadata.application.port.output;

import java.util.List;

public interface EmbeddingPort {
    List<Double> embedDocument(String text);
}
