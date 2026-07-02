package com.example.demo.constants;

public final class Constants {
    private Constants() {
        // Prevent instantiation
    }

    // File types
    public static final class FileTypes {
        public static final String PDF = "application/pdf";
        public static final String WORD = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }

    // File paths
    public static final class FilePaths {
        public static final String TESSDATA_PATH = "./tessdata";
        public static final String TESSERACT_PATH = "src/main/resources/tesseract/tessdata";
        public static final String TEST_TEMPLATE = "/templates/test_template.docx";
        public static final String IMAGE_TEMP = "src/main/resources/static/";
    }

    // File extensions
    public static final class FileExtensions {
        public static final String DOCX = ".docx";
        public static final String PDF = ".pdf";
    }

    // Languages
    public static final class Languages {
        public static final String VIETNAMESE = "vie";
    }

    // Messages
    public static final class Messages {
        public static final String PDF_ONLY = "Chỉ hỗ trợ PDF";
        public static final String PDF_WORD_ONLY = "Chỉ hỗ trợ PDF và WORD";
        public static final String FILE_GENERATION_ERROR = "Lỗi tạo file";
        public static final String TEMPLATE_NOT_FOUND = "Template not found";
        public static final String PDF_READ_ERROR = "Lỗi khi đọc file PDF: ";
        public static final String PAGE_FORMAT = "Trang %d:\n%s";
    }

    // API
    public static final class Api {
        public static final String GEMINI_MODEL = "gemini-3.1-flash-lite";
        public static final String IMAGE_MODEL = "gemini-2.0-flash-preview-image-generation";
        public static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    }

    // Question format
    public static final class QuestionFormat {
        public static final String QUESTION_COUNT = "[số lượng]:%d\n";
        public static final String DIFFICULTY_LEVEL = "[mức độ]:%d\n";
        public static final String KNOWLEDGE_TYPE = "[chế độ]:%d\n";
        public static final String IMAGE_PRESENTATION = "[hình ảnh]:%d\n";
        public static final String LANGUAGE = "[ngôn ngữ]:%s\n";
        public static final String DOCUMENT_PROVIDED = "Tài liệu được cung cấp: %s";
        public static final String CROSS_CONTEXT = "\n[TÀI LIỆU CHÉO BỔ SUNG TỪ FILE LIÊN QUAN]: %s\n(Yêu cầu: Kết hợp chéo kiến thức giữa Tài liệu cung cấp và Tài liệu chéo này để tạo câu hỏi mức độ Khó/Vận dụng/Phân tích)";
        public static final String MIN_DIFFICULT = "[min_difficulty]: %f\n";
        public static final String MAX_DIFFICULT = "[max_difficulty]: %f\n";

    }

    // Map keys
    public static final class MapKeys {
        public static final String ID = "id";
        public static final String QUESTION = "question";
        public static final String OPTION_A = "optionA";
        public static final String OPTION_B = "optionB";
        public static final String OPTION_C = "optionC";
        public static final String OPTION_D = "optionD";
        public static final String ANSWER = "answer";
        public static final String EXPLAIN = "explain";
        public static final String QUESTIONS = "questions";
    }

}
