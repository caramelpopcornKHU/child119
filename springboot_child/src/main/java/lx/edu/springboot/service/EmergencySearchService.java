package lx.edu.springboot.service;

import lx.edu.springboot.dto.EmergencyDTO;
import lx.edu.springboot.entity.Emergency;
import lx.edu.springboot.repository.EmergencyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmergencySearchService {
    
    private final EmergencyRepository emergencyRepository;

    @Value("${search.radius.km:5}")
    private Double searchRadiusKm;

    public EmergencySearchService(EmergencyRepository emergencyRepository) {
        this.emergencyRepository = emergencyRepository;
    }

    /**
     * 증상 기반 병원 검색
     */
    public List<EmergencyDTO> searchHospitalsBySymptom(String symptom, Double userLat, Double userLon) {
        log.info("병원 검색 시작 - 증상: {}, 위치: ({}, {})", symptom, userLat, userLon);
        
        // 1. DB에서 반경 내 병원 조회
        List<Emergency> hospitals = emergencyRepository.findNearbyAvailableHospitals(
                userLat, userLon, searchRadiusKm
        );
        log.info("{}km 반경 내 병원 {}개 발견", searchRadiusKm, hospitals.size());

        // 2. 증상 필터링
        List<Emergency> filtered = filterBySymptom(hospitals, symptom);
        log.info("증상 필터링 후 병원 {}개", filtered.size());

        // 3. 거리 계산 + DTO 변환
        return filtered.stream()
                .map(h -> {
                    double distance = calculateDistance(userLat, userLon, h.getY(), h.getX());
                    return EmergencyDTO.from(h, distance);
                })
                .sorted(Comparator.comparingDouble(EmergencyDTO::getDistance))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<Emergency> filterBySymptom(List<Emergency> hospitals, String symptom) {
        if (symptom == null || symptom.isEmpty()) return hospitals;
        String excludeKeyword = symptom + "불가능";
        return hospitals.stream()
                .filter(h -> {
                    if (h.getMessage() == null || h.getMessage().isEmpty()) return true;
                    return !h.getMessage().toLowerCase().contains(excludeKeyword.toLowerCase());
                })
                .collect(Collectors.toList());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
