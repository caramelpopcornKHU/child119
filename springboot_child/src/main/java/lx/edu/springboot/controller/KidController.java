package lx.edu.springboot.controller;

import lx.edu.springboot.entity.Emergency;
import lx.edu.springboot.entity.kidInformation;
import lx.edu.springboot.repository.EmergencyRepository;
import lx.edu.springboot.service.FirebaseService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/kids")
public class KidController {

    private final FirebaseService firebaseService;
    private final EmergencyRepository emergencyRepository;
    
    public KidController(FirebaseService firebaseService, EmergencyRepository emergencyRepository) {
        this.firebaseService = firebaseService;
        this.emergencyRepository = emergencyRepository;
    }

    // ✅ 부모 UID로 자녀 목록 조회
    @GetMapping("/list")
    public List<Map<String, Object>> getKids(@RequestParam String parentUid)
            throws ExecutionException, InterruptedException {
        return firebaseService.getKidsByParentUid(parentUid);
    }

    // ✅ 특정 아이 상세 정보 조회
    @GetMapping("/detail")
    public Map<String, Object> getKidDetail(
            @RequestParam String parentUid,
            @RequestParam String kidName,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon
    ) throws ExecutionException, InterruptedException {

        // 1️⃣ 아이 정보 조회
        Map<String, Object> kid = firebaseService.getKidDetail(parentUid, kidName);
        
        int birthYear = (int) kid.get("birthYear");
        int birthMonth = (int) kid.get("birthMonth");
        int birthDay = (int) kid.get("birthDay");
        
        LocalDate birthDate = LocalDate.of(birthYear, birthMonth, birthDay);
        LocalDate today = LocalDate.now();

        // 2️⃣ 나이 계산
        Period period = Period.between(birthDate, today);

        int ageYears = period.getYears();
        int ageMonths = period.getMonths();

        // 3️⃣ 주변 5km 응급실 조회
        List<Emergency> nearby = emergencyRepository.findNearbyAvailableHospitals(
                lat != null ? lat : 37.4955,
                lon != null ? lon : 126.8873,
                5.0
        );

        // 4️⃣ 나이 필터링 (이전에 만든 isAllowed와 동일)
        int ageMonthsTotal = ageYears * 12 + ageMonths;

        List<Map<String, Object>> filtered = nearby.stream()
                .filter(e -> isAllowed(e, ageYears, ageMonthsTotal)) // 🔥 변경됨
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", e.getName());
                    map.put("address", e.getAddress());
                    map.put("bed_avail", e.getBed_avail());
                    map.put("bed_total", e.getBed_total());
                    map.put("message", e.getMessage());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("kidInfo", kid);
        result.put("emergencyMessages", filtered);

        return result;
    }

    private boolean isAllowed(Emergency e, int ageYears, int ageMonthsTotal) {
        String msg = e.getMessage();
        if (msg == null || msg.isEmpty()) return true;

        // 예: "10세 미만(X)", "6세 초과(X)" 등 메시지에서 숫자 추출
        Pattern yearPattern = Pattern.compile("(\\d+)\\s*(세|살)");
        Matcher yearMatcher = yearPattern.matcher(msg);

        // ✔ 개월 패턴: "6개월", "18개월", "12월"
        Pattern monthPattern = Pattern.compile("(\\d+)\\s*(개월|월)");
        Matcher monthMatcher = monthPattern.matcher(msg);

        if (yearMatcher.find()) {
            int limitYears = Integer.parseInt(yearMatcher.group(1));

            if (msg.contains("미만")) return ageYears < limitYears;
            if (msg.contains("초과")) return ageYears > limitYears;
            if (msg.contains("이하")) return ageYears <= limitYears;
            if (msg.contains("이상")) return ageYears >= limitYears;

            return true;
        }

        // 🔹 2) "개월/월" 기준 제한 확인
        if (monthMatcher.find()) {
            int limitMonths = Integer.parseInt(monthMatcher.group(1));

            if (msg.contains("미만")) return ageMonthsTotal < limitMonths;
            if (msg.contains("초과")) return ageMonthsTotal > limitMonths;
            if (msg.contains("이하")) return ageMonthsTotal <= limitMonths;
            if (msg.contains("이상")) return ageMonthsTotal >= limitMonths;

            return true;
        }

        // 제한이 명확하지 않은 경우 허용
        return true;
    }
}