package com.example.demo.service.iservice;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;

public interface IQuizService {
    StateResponse<Object> privateHandlePdf(MultipartFile file, int questionCount, int mode, int type,
            String language) throws Exception;

    StateResponse<Object> publicHandlePdf(MultipartFile file, int questionCount, int mode, int type,
            String language) throws Exception;

    StateResponse<Object> handleBasePdf(MultipartFile file, int questionCount, int mode, int type, int imgQuest,
            String language);

    StateResponse<Object> handleScanPdf(MultipartFile file, int questionCount, int mode, int type, int imgQuest,
            String language);

    String combinePrompt(int questionCount, int mode, String pdfText, int type, int imgQuesion,
            String language);
}
