package com.example.demo.modules.document.progress.application.usecase.query;

import org.springframework.stereotype.Service;

import com.example.demo.modules.document.progress.infrastructure.adapter.ReadingProgressCacheAdapter;
import com.example.demo.modules.document.progress.api.dto.ReadingProgressResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProgressUseCase {

    private final ReadingProgressCacheAdapter cacheAdapter;

    public ReadingProgressResponse execute(Long userId, Long pdfId) {
        return cacheAdapter.getProgress(userId, pdfId);
    }
}
