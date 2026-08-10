package com.example.demo.modules.document.processing.application.port.output;

import java.util.List;

public interface TokenizerPort {
    List<String> tokenize(String text);
}
