

## Giai đoạn 1: Chuẩn bị Mô hình & Hạ tầng
Vì bạn không có GPU mạnh, mục tiêu là sử dụng các mô hình đã được tối ưu hóa.

* **Lựa chọn Model:** Ưu tiên **Llama 3 (8B)** hoặc **Phi-3-mini**. Đây là những model nhỏ nhưng cực kỳ thông minh, đủ sức hiểu tiếng Việt và cấu trúc JSON.
* **Kỹ thuật Nén (Quantization):** Chốt sử dụng định dạng **4-bit (GGUF)**. Nó giúp giảm dung lượng model từ 16GB xuống còn khoảng 5GB, chạy được trên RAM máy tính thông thường.
* **Công cụ chạy mô hình (Inference Engine):** Cài đặt **Ollama**. Đây là công cụ nhẹ nhất và dễ nhất để biến một file model thành một API endpoint cho Java gọi vào.

---

## Giai đoạn 2: Tạo Dataset & Finetuning
Đây là bước để "dạy" model nhỏ hoạt động chuyên nghiệp như Gemini.

* **Xây dựng Dataset:**
    * Số lượng: Khoảng **200 - 500 mẫu** chất lượng cao.
    * Cấu trúc: Một cặp gồm {`Văn bản đầu vào` + `Yêu cầu`} và {`Output JSON câu hỏi`}.
* **Thực hiện Finetune (QLoRA):**
    * Sử dụng **Unsloth** trên Google Colab (tận dụng GPU miễn phí của Google).
    * Kết quả thu được là một file **Adapter** (rất nhẹ, chỉ vài chục MB) chứa "tư duy" tạo câu hỏi của bạn.
* **Đóng gói:** Hợp nhất (Merge) Adapter vào model 4-bit gốc và xuất ra file `.gguf` cuối cùng.

---

## Giai đoạn 3: Xử lý tài liệu dài (Input > 50.000 tokens)
Đây là phần quan trọng nhất để đảm bảo chất lượng câu hỏi khi không thể nạp toàn bộ file vào RAM.

* **Kỹ thuật Chia nhỏ (Chunking):** Chia tài liệu thành các đoạn khoảng 1000 - 2000 tokens. 
* **Overlap (Gối đầu):** Giữ lại khoảng 200 tokens cuối của đoạn trước cho đoạn sau để AI không mất ngữ cảnh.
* **Quy trình 2 bước (Map-Reduce):**
    1.  **Map:** Cho AI đọc từng đoạn để lấy ý chính (Summary).
    2.  **Reduce:** Dùng các bản tóm tắt đó để tạo ra các câu hỏi mang tính tổng hợp toàn tài liệu.



---

## Giai đoạn 4: Cấu hình Backend (Java/Spring Boot)
Chỉnh sửa code hiện tại của FreeQuizAI để kết nối với "đầu não" mới.

* **Thay đổi Dependency:** Nếu dùng Spring AI, thay `spring-ai-google-gemini` bằng `spring-ai-ollama`.
* **Cấu hình tham số (Inference Parameters):**
    * `Temperature`: Đặt mức **0.1 - 0.2** (để câu hỏi luôn chính xác, không "chế" lời).
    * `Top-P`: Khoảng **0.9**.
* **Kết nối API:** Trỏ URL về `http://localhost:11434` (Ollama local) thay vì API của Google.

---

## Giai đoạn 5: Kiểm định & Tối ưu (Evaluation)
* **Kiểm tra Format:** Đảm bảo output của Custom LLM luôn là JSON hợp lệ (bước Finetune ở Giai đoạn 2 sẽ quyết định việc này).
* **Hậu xử lý (Post-processing):** Viết logic Java để lọc bỏ các câu hỏi trùng lặp nếu tài liệu quá dài và bị lặp ý giữa các chunk.
* **Prompt Engineering:** Tinh chỉnh lại System Prompt trong file cấu hình của Ollama (Modelfile) để định hướng phong cách đặt câu hỏi (khó/dễ, tiếng Việt/tiếng Anh).

---

### Tóm tắt tài nguyên bạn cần dùng:
1.  **Hardware:** Máy tính có tối thiểu 8GB - 16GB RAM.
2.  **Training:** Google Colab (Miễn phí).
3.  **Library:** Unsloth (Python) để train, LangChain4j hoặc Spring AI (Java) để kết nối.
4.  **Runtime:** Ollama.

Việc này sẽ giúp bạn sở hữu một hệ thống AI hoàn toàn riêng tư, không tốn phí gọi API hàng tháng và hoạt động ổn định trên hạ tầng sẵn có.

**Bạn đã sẵn sàng để bắt đầu bước đầu tiên là chuẩn bị Dataset chưa? Tôi có thể hướng dẫn bạn cách dùng chính Gemini hiện tại để "đẻ" ra Dataset cho mô hình mới.**
