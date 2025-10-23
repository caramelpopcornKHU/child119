package lx.edu.springboot.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lx.edu.springboot.service.ChatService;
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

    // WebSocketSession ID별로 멀티턴 히스토리를 저장
    private final Map<String, List<String>> sessionHistories = new ConcurrentHashMap<>();

    public ChatHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 새로운 세션 생성 시 히스토리 초기화
        sessionHistories.put(session.getId(), new ArrayList<>());
        System.out.println("새로운 연결: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userMessage = message.getPayload();
        System.out.println(session.getId() + "로부터 받은 메시지: " + userMessage);

        // ChatService를 통해 AI 응답 받기
        String aiResponse = chatService.getAiResponse(session.getId(), userMessage);

        System.out.println("AI 응답: " + aiResponse);

        // 클라이언트에 전송
        session.sendMessage(new TextMessage(aiResponse));
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 세션 종료 시 히스토리 제거
        sessionHistories.remove(session.getId());
        System.out.println("연결 종료: " + session.getId());
    }
}
