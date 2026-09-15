package duan.sportify.controller;

import duan.sportify.dao.FieldDAO;
import duan.sportify.dto.ChatRequest;
import duan.sportify.dto.ChatResponse;
import duan.sportify.dto.FieldDTO;
import duan.sportify.entities.Field;
import duan.sportify.service.GeminiAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatbotRestController {

    @Autowired
    private GeminiAiService geminiAiService;

    @Autowired
    private FieldDAO fieldDAO;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request, javax.servlet.http.HttpSession session) {
        try {
            String userMessage = request.getMessage();
            String context = request.getContext() != null ? request.getContext() : "";
            
            // Lấy lịch sử chat từ Session (giữ tối đa 10 tin nhắn gần nhất)
            List<Map<String, String>> sessionHistory = (List<Map<String, String>>) session.getAttribute("sportai_history");
            List<Map<String, String>> chatHistory = new ArrayList<>();
            if (sessionHistory != null) {
                chatHistory.addAll(sessionHistory);
            }
            
            // Thêm tin nhắn mới của User vào lịch sử
            Map<String, String> userMsgMap = new HashMap<>();
            userMsgMap.put("role", "user");
            userMsgMap.put("content", userMessage);
            chatHistory.add(userMsgMap);
            
            // Giữ cho lịch sử không quá dài (tránh lỗi API Token Limit)
            if (chatHistory.size() > 10) {
                chatHistory = new ArrayList<>(chatHistory.subList(chatHistory.size() - 10, chatHistory.size()));
            }

            // Fetch all active fields to pass to the AI as context
            List<Field> fields = fieldDAO.findAllActive();

            // 1. Get AI Response (truyền lịch sử chat + thông tin sân)
            String aiText = geminiAiService.getChatResponse(chatHistory, fields, context);
            
            // Thêm tin nhắn phản hồi của AI vào lịch sử
            Map<String, String> aiMsgMap = new HashMap<>();
            aiMsgMap.put("role", "assistant");
            aiMsgMap.put("content", aiText);
            chatHistory.add(aiMsgMap);
            
            // Lưu lại session
            session.setAttribute("sportai_history", chatHistory);
            
            // 2. Check if intent is searching for fields
            List<FieldDTO> fieldDTOs = new ArrayList<>();
            boolean isSearching = aiText != null && aiText.contains("[SHOW_FIELDS");
            List<Integer> aiSelectedIds = new ArrayList<>();
            
            if (isSearching) {
                int start = aiText.indexOf("[SHOW_FIELDS");
                int end = aiText.indexOf("]", start);
                if (start != -1 && end != -1) {
                    String idsStr = aiText.substring(start + 12, end).replaceAll("[^0-9,]", "");
                    for (String id : idsStr.split(",")) {
                        if (!id.trim().isEmpty()) {
                            try { aiSelectedIds.add(Integer.parseInt(id.trim())); } catch (Exception ignored) {}
                        }
                    }
                    aiText = aiText.substring(0, start).trim();
                } else {
                    aiText = aiText.replace("[SHOW_FIELDS]", "").trim();
                }
            }
            
            if (isSearching) {
                List<Field> filteredFields = new ArrayList<>();
                
                if (!aiSelectedIds.isEmpty()) {
                    for (Field f : fields) {
                        if (aiSelectedIds.contains(f.getFieldid())) {
                            filteredFields.add(f);
                        }
                    }
                }
                
                // Fallback nếu AI không gửi ID nào hoặc sai ID
                if (filteredFields.isEmpty()) {
                    filteredFields = fields.stream().limit(3).collect(Collectors.toList());
                }

                fieldDTOs = filteredFields.stream().map(f -> new FieldDTO(
                        f.getFieldid(),
                        f.getNamefield(),
                        f.getPrice(),
                        f.getImage(),
                        f.getAddress()
                )).collect(Collectors.toList());
            }

            // 3. Return combined response
            ChatResponse response = new ChatResponse(aiText, fieldDTOs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ChatResponse("Lỗi Backend: " + e.toString(), new ArrayList<>()));
        }
    }
}
