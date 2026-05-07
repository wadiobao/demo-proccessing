package com.example.demo.modules.document.progress.application.usecase.command;

import org.springframework.stereotype.Service;

import com.example.demo.modules.document.progress.infrastructure.adapter.ReadingProgressCacheAdapter;
import com.example.demo.modules.document.progress.api.dto.ReadingProgressRequest;
import com.example.demo.modules.document.progress.api.dto.ReadingProgressResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaveProgressUseCase {

    private final ReadingProgressCacheAdapter cacheAdapter;

    public ReadingProgressResponse execute(Long userId, ReadingProgressRequest request) {
        return cacheAdapter.saveProgress(userId, request);
    }
}
