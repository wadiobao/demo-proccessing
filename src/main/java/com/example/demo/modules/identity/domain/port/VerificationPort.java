package com.example.demo.modules.identity.domain.port;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface VerificationPort {
    void store(String key, String value, long timeout, TimeUnit unit);
    Optional<String> get(String key);
    void delete(String key);
}
