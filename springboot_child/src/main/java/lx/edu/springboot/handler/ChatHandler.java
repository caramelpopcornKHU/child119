package lx.edu.springboot.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lx.edu.springboot.service.ChatService;
import lx.edu.springboot.service.FirebaseService;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FirebaseService firebaseService;

    // WebSocketSession ID별로 멀티턴 히스토리를 저장
    private final Map<String, List<String>> sessionHistories = new ConcurrentHashMap<>();


    public ChatHandler(ChatService chatService, FirebaseService firebaseService) {
        this.chatService = chatService;
        this.firebaseService = firebaseService;
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 새로운 세션 생성 시 히스토리 초기화
        sessionHistories.put(session.getId(), new ArrayList<>());
        System.out.println("새로운 연결: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> request = objectMapper.readValue(message.getPayload(), Map.class);
        String userMessage = (String) request.get("message");
        Map<String, Object> kidInfo = (Map<String, Object>) request.get("kidInfo");
        Double lat = request.get("lat") != null ? ((Number) request.get("lat")).doubleValue() : null;
        Double lon = request.get("lon") != null ? ((Number) request.get("lon")).doubleValue() : null;

        Map<String, Object> detailResult = null;
        if (kidInfo != null) {
            String parentUid = (String) kidInfo.get("parentUid");
            String kidName = (String) kidInfo.get("kid");

            // ✅ FirebaseService 인스턴스로 호출
            detailResult = firebaseService.getKidDetail(parentUid, kidName);
        }

        String aiText = chatService.getAiResponse(session.getId(), userMessage, lat, lon, kidInfo);

        Map<String, Object> response = new HashMap<>();
        response.put("aiText", aiText);
        response.put("kidDetail", detailResult != null ? detailResult : Collections.emptyMap());

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }



    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionHistories.remove(session.getId());
        System.out.println("연결 종료: " + session.getId());
    }
}
