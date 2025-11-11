package lx.edu.springboot.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lx.edu.springboot.entity.Emergency;
import lx.edu.springboot.repository.EmergencyRepository;

@Service
public class ChatService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private static final String SYSTEM_PROMPT =
            "너는 의료 전문가야. 의사처럼 친절하고 정확하게 설명해줘. " +
            "너는 의료 전문가니깐 점메추, 저메추 이런거 대답하지마" +
            "근처 응급실 추천 시 구로구청 기준으로 안내해줘. " +
            "응급처치 방법을 질문받으면, 답변의 시작과 끝에 불필요한 인사말이나 감정 표현, " +
            "서론/결론 문구를 일절 넣지 마. 각 단계를 번호 매김 목록(1., 2., 3., ...) 형태로만 " +
            "내용을 제공하고 각 항목 끝에 줄바꿈(엔터)을 포함해줘. " +
            "응급실 추천 등 다른 질문일 때는 친절하고 정확한 의사 말투를 유지해. " +
            "불필요한 말은 절대 하지 마."+ "정말 간략하게 말해줘";

    private final ConcurrentHashMap<String, List<String>> sessionHistories = new ConcurrentHashMap<>();

    @Autowired
    private EmergencyRepository emergencyRepository;

    public ChatService(EmergencyRepository emergencyRepository) {
        this.emergencyRepository = emergencyRepository;
    }

    public String getAiResponse(String sessionId, String userMessage, Double lat, Double lon, Map<String, Object> kidInfo) {
        List<String> history = sessionHistories.computeIfAbsent(sessionId, k -> new ArrayList<>());

        double baseLat = (lat != null) ? lat : 37.4955;
        double baseLon = (lon != null) ? lon : 126.8873;

        String emergencyInfo = getNearbyEmergencyInfo(userMessage, baseLat, baseLon);

        StringBuilder prompt = new StringBuilder();
        prompt.append(SYSTEM_PROMPT).append("\n\n");

        int start = Math.max(0, history.size() - 5);
        for (int i = start; i < history.size(); i++) {
            prompt.append(history.get(i)).append("\n");
        }

        if (emergencyInfo != null && !emergencyInfo.isEmpty()) {
            prompt.append("현재 위치 기준 응급실 정보:\n").append(emergencyInfo).append("\n\n");
        }

        // ✅ kidInfo가 null이 아닐 때만 추가
        if (kidInfo != null) {
            prompt.append("아이 정보: ").append(kidInfo).append("\n");
        }

        prompt.append("사용자: ").append(userMessage);

        String aiResponse = callGeminiApi(prompt.toString());

        history.add("사용자: " + userMessage);
        history.add("AI: " + aiResponse);
        if (history.size() > 5) history.subList(0, history.size() - 5).clear();

        return aiResponse;
    }



    private String getNearbyEmergencyInfo(String userMessage, double lat, double lon) {
        List<Emergency> nearby = emergencyRepository.findNearbyAvailableHospitals(lat, lon, 5.0);

        // state=1,2 & bed_avail > 0 필터 (String → int 변환)
        nearby = nearby.stream()
                .filter(e -> (e.getState() == 1 || e.getState() == 2) && parseBed(e.getBed_avail()) > 0)
                .filter(e -> isSymptomAllowed(userMessage, e.getMessage()))
                .limit(3)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        for (Emergency e : nearby) {
            sb.append(String.format(
                "🏥 %s\n주소: %s\n병상: %s/%s\n메시지: %s\n\n",
                e.getName(), e.getAddress(), e.getBed_avail(), e.getBed_total(), e.getMessage() != null ? e.getMessage() : "없음"
            ));
        }
        return sb.toString();
    }

    // bed_avail, bed_total 문자열 → 숫자 변환 안전하게
    private int parseBed(String bed) {
        try {
            return Integer.parseInt(bed.trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    private boolean isSymptomAllowed(String userSymptom, String hospitalMessage) {
        if (hospitalMessage == null || hospitalMessage.isEmpty()) return true;
        double similarity = computeCosineSimilarity(userSymptom, hospitalMessage);
        double threshold = 0.3;
        return similarity < threshold;
    }

    private double computeCosineSimilarity(String text1, String text2) {
        double[] vec1 = embeddingModel(text1);
        double[] vec2 = embeddingModel(text2);
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            dot += vec1[i] * vec2[i];
            normA += vec1[i] * vec1[i];
            normB += vec2[i] * vec2[i];
        }
        return normA == 0 || normB == 0 ? 0.0 : dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double[] embeddingModel(String text) {
        // TODO: 실제 임베딩 API 호출
        return new double[768];
    }

    private String extractLocationFromMessage(String text) {
        String[] knownLocations = {"구로", "강남", "강동", "송파", "은평", "마포", "종로", "서초", "관악"};
        for (String loc : knownLocations) {
            if (text.contains(loc)) return loc;
        }
        return null;
    }

    private String callGeminiApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(textPart));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        String requestUrl = apiUrl + "?key=" + apiKey;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(requestUrl, entity, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    Map<String, Object> part = ((List<Map<String, Object>>) contentMap.get("parts")).get(0);
                    return (String) part.get("text");
                }
            }
            return "AI 응답을 처리하는 중 오류가 발생했습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            return "AI 서비스 호출에 실패했습니다.";
        }
    }

    public String sendMessage(String userMessage) {
        String prompt = SYSTEM_PROMPT + "\n\n사용자: " + userMessage;
        return callGeminiApi(prompt);
    }

}
