package lx.edu.springboot.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatService {

    // HTTP 요청용
    private final RestTemplate restTemplate = new RestTemplate();

    // Gemini API 키 & URL
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private static final String SYSTEM_PROMPT =
            "너는 의료 전문가야. 의사처럼 친절하고 정확하게 설명해줘. " +
            "근처 응급실 추천 시 구로구청 기준으로 안내해줘. " +
            "응급처치 방법을 질문받으면, 답변의 시작과 끝에 불필요한 인사말이나 감정 표현, " +
            "서론/결론 문구를 일절 넣지 마. 각 단계를 번호 매김 목록(1., 2., 3., ...) 형태로만 " +
            "내용을 제공하고 각 항목 끝에 줄바꿈(엔터)을 포함해줘. " +
            "응급실 추천 등 다른 질문일 때는 친절하고 정확한 의사 말투를 유지해. " +
            "불필요한 말은 절대 하지 마."+
            "정말 간략하게 말해줘";
    
    // WebSocket 세션별 히스토리 저장
    private final ConcurrentHashMap<String, List<String>> sessionHistories = new ConcurrentHashMap<>();

    /**
     * 멀티턴 대화용
     */
    public String getAiResponse(String sessionId, String userMessage) {
        List<String> history = sessionHistories.computeIfAbsent(sessionId, k -> new ArrayList<>());

        // 히스토리 포함 프롬프트 생성
        StringBuilder prompt = new StringBuilder();
        prompt.append(SYSTEM_PROMPT).append("\n\n");  // 역할 프롬프트 먼저 추가
        for (String h : history) {
            prompt.append(h).append("\n");
        }
        prompt.append("사용자: ").append(userMessage);

        // Gemini API 호출
        String aiResponse = callGeminiApi(prompt.toString());

        // 대화 기록 저장
        history.add("사용자: " + userMessage);
        history.add("AI: " + aiResponse);

        return aiResponse;
    }

    /**
     * Gemini API 호출 (기존 단일 메시지 호출과 동일)
     */
    private String callGeminiApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format(
            "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
            prompt
        );

        
        String requestUrl = apiUrl + "?key=" + apiKey;
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(requestUrl, entity, Map.class);

            if (response.getBody() != null) {
                Map<String, Object> candidate = ((List<Map<String, Object>>) response.getBody().get("candidates")).get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                Map<String, Object> part = ((List<Map<String, Object>>) content.get("parts")).get(0);
                return (String) part.get("text");
            }

            return "AI 응답을 처리하는 중 오류가 발생했습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            return "AI 서비스 호출에 실패했습니다.";
        }
    }
    public String sendMessage(String userMessage) {
        return callGeminiApi(SYSTEM_PROMPT + "\n\n사용자: " + userMessage);
    }
    
    
}
