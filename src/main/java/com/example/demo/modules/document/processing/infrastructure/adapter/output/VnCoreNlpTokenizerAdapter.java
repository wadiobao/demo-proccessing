package com.example.demo.modules.document.processing.infrastructure.adapter.output;

import com.example.demo.modules.document.processing.application.port.output.TokenizerPort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VnCoreNlpTokenizerAdapter implements TokenizerPort {

    // private final VnCoreNLP vnCoreNLP; 

    public VnCoreNlpTokenizerAdapter() {
        // try {
        //     vnCoreNLP = new VnCoreNLP(new String[]{"wseg", "pos", "ner"});
        // } catch (Exception e) {
        //     throw new RuntimeException("Lỗi load VnCoreNLP", e);
        // }
    }

    @Override
    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text != null && !text.isBlank()) {
            String[] words = text.split("\\s+");
            for (String word : words) {
                tokens.add(word);
            }
        }
        return tokens;
    }
}
