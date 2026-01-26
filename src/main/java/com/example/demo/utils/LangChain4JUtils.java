package com.example.demo.utils;

import dev.langchain4j.service.SystemMessage;

public class LangChain4JUtils {

	interface Assistant {
        @SystemMessage("""
        		Bạn là một chuyên gia phân tích nội dung. Hãy đọc tài liệu dưới đây và trích xuất thông tin theo cấu trúc JSON như sau:
				topic_id: Mã định danh chủ đề bằng tiếng Anh (viết thường, dùng dấu gạch nối, ví dụ: 'artificial-intelligence', 'healthy-lifestyle'). Mã này phải nhất quán cho các tài liệu cùng chủ đề.
				topic_display: Tên chủ đề ngắn gọn (dưới 7 từ) bằng ngôn ngữ của tài liệu.
				tags: Danh sách tối đa 10 từ khóa quan trọng nhất xuất hiện trong bài.
				language: Mã ngôn ngữ của tài liệu (ví dụ: 'vi', 'en').
				Yêu cầu: Chỉ trả về kết quả định dạng JSON thuần túy, không giải thích gì thêm.
        		""")
        String detectTopicAndTags(String code);
    }
	
}
