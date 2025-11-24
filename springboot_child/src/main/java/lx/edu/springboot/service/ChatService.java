package lx.edu.springboot.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            "너는 의료 전문가야. 의사처럼 친절하고 정확하게 설명해줘." +
            "너는 의료 전문가니깐 점메추, 저메추 이런 거 대답하지 마." +
            "근처 응급실 추천 시 구로구청 기준으로 안내해줘." +
            "응급실 정보를 제공받은 경우 반드시 전체 병상 수, 병상 여유량, 응급실 공지 메시지를 그대로 참고해서 답변해줘." +
            "응급실 정보가 주어졌으면 그것을 기반으로 가장 적절한 응급실을 선택해 설명해야 해." +
            "응급처치 방법을 질문받으면, 답변의 시작과 끝에 불필요한 인사말이나 감정 표현, 서론/결론 문구를 일절 넣지 마." +
            "각 단계를 번호 매김 목록(1., 2., 3., ...) 형태로만 제공하고 각 항목 끝에 줄바꿈(엔터)을 포함해줘." +
            "응급처치 방법은 반드시 영등포구 보건소 응급처치 가이드(https://www.ydp.go.kr/health/contents.do?key=3594&)의 기준에 따라 설명해줘." +
            "응급실 추천 등 다른 질문일 때는 친절하고 정확한 의사 말투를 유지해." +
            "불필요한 말은 절대 하지 마." +
            "정말 간략하게 말해줘.";


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

        StringBuilder prompt = new StringBuilder();
        prompt.append(SYSTEM_PROMPT).append("\n\n");

        int start = Math.max(0, history.size() - 5);
        for (int i = start; i < history.size(); i++) {
            prompt.append(history.get(i)).append("\n");
        }

        // ✅ 아이 정보가 있으면 나이 계산 후 프롬프트에 추가
        if (kidInfo != null) {
            String ageInfo = calculateAgeInfo(kidInfo);
            if (ageInfo != null) {
                prompt.append(ageInfo).append("\n");
            }
        }

        // ✅ 응급실 질문 + 아이 정보 → 연령 메시지 추가
        if (isEmergencyRoomQuery(userMessage) && kidInfo != null) {
            String ageNotice = generateAgeNotice(kidInfo, baseLat, baseLon);
            if (ageNotice != null) {
                prompt.append("\n연령 제한 분석: ").append(ageNotice).append("\n");
            }
        }

        // 🛠️ 🔥🔥 여기 추가 → 응급실 정보 프롬프트 삽입
        if (isEmergencyRoomQuery(userMessage)) {
            String emergencyInfo = getNearbyEmergencyInfo(userMessage, baseLat, baseLon);
            prompt.append("\n\n[근처 응급실 정보]\n").append(emergencyInfo).append("\n");
        }


        prompt.append("사용자: ").append(userMessage);

        String aiResponse = callGeminiApi(prompt.toString());
        if (kidInfo != null) {
            String ageInfo = calculateAgeInfo(kidInfo);
            if (ageInfo != null) {
                aiResponse = ageInfo + "\n\n" + aiResponse;
            }
        }
        if (isEmergencyRoomQuery(userMessage)) {
            String emergencyInfo = getNearbyEmergencyInfo(userMessage, baseLat, baseLon);
            aiResponse = "[근처 응급실 정보]\n" + emergencyInfo + "\n" + aiResponse;
        }
        history.add("사용자: " + userMessage);
        history.add("AI: " + aiResponse);
        if (history.size() > 5) history.subList(0, history.size() - 5).clear();

        return aiResponse;
    }

    // ✅ 나이 계산 메서드 추가
    private String calculateAgeInfo(Map<String, Object> kidInfo) {
        try {
            // Long을 int로 안전하게 변환
            int birthYear = ((Number) kidInfo.get("birthYear")).intValue();
            int birthMonth = ((Number) kidInfo.get("birthMonth")).intValue();
            int birthDay = ((Number) kidInfo.get("birthDay")).intValue();
            String kidName = (String) kidInfo.get("kid");

            LocalDate birthDate = LocalDate.of(birthYear, birthMonth, birthDay);
            LocalDate today = LocalDate.now();
            Period period = Period.between(birthDate, today);

            int ageYears = period.getYears();
            int ageMonths = period.getMonths();

            if (ageYears >= 1) {
                return String.format("현재 선택된 아이(%s)는 만 %d세 %d개월입니다. 이 나이를 기준으로 응답합니다.", 
                                    kidName, ageYears, ageMonths);
            } else {
                int totalMonths = ageYears * 12 + ageMonths;
                return String.format("현재 선택된 아이(%s)는 생후 %d개월입니다. 이 나이를 기준으로 응답합니다.", 
                                    kidName, totalMonths);
            }
        } catch (Exception e) {
            System.err.println("나이 계산 실패: " + e.getMessage());
            return null; // 나이 계산 실패 시 null 반환
        }
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
    
    private boolean isEmergencyRoomQuery(String userMessage) {
        if (userMessage == null) return false;

        userMessage = userMessage.replace(" ", "").toLowerCase();

        String[] keywords = {
                "응급실", "병원", "응급", "어디", "어딜",
                "가까운", "빨리가", "병상", "진료가능", "이송",
                "소요시간", "네비", "길찾기"
        };

        for (String key : keywords) {
            if (userMessage.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private String generateAgeNotice(Map<String, Object> kidInfo, double lat, double lon) {
        try {
            // ✅ Long을 int로 안전하게 변환
            int birthYear = ((Number) kidInfo.get("birthYear")).intValue();
            int ageYears = LocalDate.now().getYear() - birthYear;

            List<Emergency> nearby = emergencyRepository.findNearbyAvailableHospitals(lat, lon, 5.0);

            int allowed = 0;
            int denied = 0;

            for (Emergency e : nearby) {
                if (isAllowedAge(e.getMessage(), ageYears)) allowed++;
                else denied++;
            }

            if (denied == 0) {
                return "현재 선택한 아이 기준으로 주변 응급실 연령 제한 메시지는 없지만, 방문 전 전화 확인을 추천드립니다.";
            } else if (allowed > 0 && denied > 0) {
                return "일부 응급실에서 연령 제한이 있어 선택 시 주의가 필요합니다. 방문 전 전화 확인을 권장합니다.";
            } else {
                return "현재 선택한 아이 기준으로 소아 진료가 제한된 응급실이 많습니다. 방문 가능 여부를 반드시 전화로 확인하세요.";
            }
        } catch (Exception e) {
            System.err.println("나이 통지 생성 실패: " + e.getMessage());
            return null;
        }
    }
    private boolean isAllowedAge(String msg, int ageYears) {
        if (msg == null || msg.isEmpty()) return true;

        Pattern p = Pattern.compile("(\\d+)\\s*(세|살)");
        Matcher m = p.matcher(msg);

        if (m.find()) {
            int value = Integer.parseInt(m.group(1));
            if (msg.contains("미만")) return ageYears < value;
            if (msg.contains("초과")) return ageYears > value;
            if (msg.contains("이하")) return ageYears <= value;
            if (msg.contains("이상")) return ageYears >= value;
        }
        return true;
    }
}