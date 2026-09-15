package duan.sportify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import duan.sportify.entities.Field;

@Service
public class GeminiAiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getChatResponse(List<Map<String, String>> messageHistory, List<Field> allFields, String contextMsg) {
        if(geminiApiKey == null || geminiApiKey.contains("YOUR_API_KEY") || geminiApiKey.startsWith("AQ.")) {
            return "Xin chào! Mình là SportAI. (Vui lòng điền mã OpenRouter API Key vào application.properties thay cho mã cũ nhé!)";
        }
        
        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("Bạn là SportAI, nhân viên tư vấn đặt sân bóng chuyên nghiệp của hệ thống Sportify. ");
        systemPrompt.append("Quy định quan trọng: Khách đặt sân phải CỌC TRƯỚC 30% tiền sân (không phải 50%). ");
        systemPrompt.append("Dưới đây là danh sách các sân hiện có (Tên sân - Giá - Địa chỉ):\n");
        for (int i = 0; i < Math.min(allFields.size(), 50); i++) {
            Field f = allFields.get(i);
            systemPrompt.append("- ID: ").append(f.getFieldid()).append(" | Tên: ").append(f.getNamefield()).append(" | Giá: ").append(f.getPrice()).append(" VNĐ | Địa chỉ: ").append(f.getAddress()).append("\n");
        }
        systemPrompt.append("\nHãy trả lời ngắn gọn, lịch sự, xưng hô 'mình' và 'bạn'. LUÔN LUÔN trả lời bằng Tiếng Việt. KHÔNG hiển thị quá trình suy nghĩ (như <think>...</think>), chỉ đưa ra câu trả lời trực tiếp cuối cùng. ");
        systemPrompt.append("LƯU Ý 1: Nếu khách hỏi địa chỉ hoặc giá của một sân cụ thể, hãy dò tìm tên sân trong danh sách trên để trả lời chính xác. Nếu không thấy, hãy xin lỗi và báo là chưa cập nhật. ");
        systemPrompt.append("LƯU Ý 2: Bạn KHÔNG CÓ dữ liệu lịch trống của sân. Nếu khách hỏi lịch trống (ngày/giờ cụ thể), TUYỆT ĐỐI không bịa ra lịch, hãy khuyên khách xem trực tiếp bảng lịch trên giao diện trang chi tiết sân. ");
        systemPrompt.append("MÃ LỆNH QUAN TRỌNG [SHOW_FIELDS: ID1, ID2, ...]: Bất cứ khi nào bạn giới thiệu, gợi ý, hoặc liệt kê sân cho khách (ví dụ: khách tìm sân, hỏi sân nào rẻ, sân nào ở quận X), bạn BẮT BUỘC PHẢI chèn mã này ở DÒNG CUỐI CÙNG để hệ thống tạo nút đặt sân. Ví dụ: [SHOW_FIELDS: 1, 3, 5]. TUYỆT ĐỐI KHÔNG in chữ 'ID' ra màn hình chat với khách. LƯU Ý TRÁI NGƯỢC: Nếu khách chỉ hỏi về 1 sân cụ thể (vị trí/giá của sân họ đang xem) hoặc hỏi luật/thanh toán, TUYỆT ĐỐI KHÔNG DÙNG MÃ NÀY.");
        
        String[] fallbackModels = {
            "google/gemini-2.0-flash-lite-preview-02-05:free",
            "google/gemma-2-9b-it:free",
            "huggingfaceh4/zephyr-7b-beta:free",
            "openrouter/free"
        };
        
        Exception lastException = null;

        for (String modelName : fallbackModels) {
            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", modelName); 
                requestBody.put("max_tokens", 1000);
                
                // Build messages array
                List<Map<String, String>> messages = new java.util.ArrayList<>();
                Map<String, String> sysMsg = new HashMap<>();
                sysMsg.put("role", "system");
                sysMsg.put("content", systemPrompt.toString() + (contextMsg.isEmpty() ? "" : "\n" + contextMsg));
                messages.add(sysMsg);
                
                // Add history
                messages.addAll(messageHistory);
                
                requestBody.put("messages", messages);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + geminiApiKey);
                headers.set("HTTP-Referer", "http://localhost:8080"); 
                headers.set("X-Title", "Sportify");

                HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
                
                String responseStr = restTemplate.postForObject(OPENROUTER_API_URL, request, String.class);
                JsonNode root = objectMapper.readTree(responseStr);
                JsonNode choices = root.path("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    String answer = choices.get(0).path("message").path("content").asText();
                    // Loại bỏ thẻ <think>...</think> nếu có của các model suy luận (DeepSeek)
                    if (answer.contains("<think>")) {
                        answer = answer.replaceAll("(?s)<think>.*?</think>", "").trim();
                    }
                    return answer;
                }
                return "Xin lỗi, hệ thống AI không trả về kết quả.";
            } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                lastException = e;
                continue; // Thử model tiếp theo
            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                if (e.getStatusCode().value() != 401) {
                    lastException = e;
                    continue; // Thử model tiếp theo nếu không phải lỗi sai API Key
                }
                e.printStackTrace();
                return "Lỗi API Key không hợp lệ hoặc đã hết hạn!";
            } catch (Exception e) {
                lastException = e;
                continue; // Thử tiếp
            }
        }
        
        String errorDetails = lastException != null ? lastException.getMessage() : "Unknown error";
        if (lastException instanceof org.springframework.web.client.HttpStatusCodeException) {
            errorDetails = ((org.springframework.web.client.HttpStatusCodeException) lastException).getResponseBodyAsString();
        }
        
        if (errorDetails.contains("free-models-per-day")) {
            return "Xin lỗi bạn, khóa API (API Key) hiện tại đã hết lượt dùng miễn phí trong ngày (giới hạn 50 lần/ngày). Vui lòng đổi API Key mới hoặc quay lại vào ngày mai nhé!";
        }
        
        return "Xin lỗi bạn, tất cả các máy chủ AI miễn phí hiện đang quá tải hoặc từ chối kết nối. Chi tiết lỗi từ máy chủ cuối cùng: " + errorDetails;
    }
}
