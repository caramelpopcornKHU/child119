package lx.edu.springboot.repository;

import lx.edu.springboot.entity.Emergency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, Long> {
    
    /**
     * 5km 반경 내 가용 응급실 조회 (state=1,2만, 거리순 정렬)
     * Haversine 공식 사용
     */
    @Query(value = """
		SELECT *
		FROM (
		    SELECT e.*,
		           (6371 * acos(
		               cos(radians(:lat)) * cos(radians(e.y)) *
		               cos(radians(e.x) - radians(:lon)) +
		               sin(radians(:lat)) * sin(radians(e.y))
		           )) AS distance
		    FROM emergency e
		    WHERE e.state IN (1, 2)
		) AS sub
		WHERE distance <= :radiusKm
		ORDER BY distance
        """, nativeQuery = true)
    List<Emergency> findNearbyAvailableHospitals(
            @Param("lat") Double lat,
            @Param("lon") Double lon,
            @Param("radiusKm") Double radiusKm
    );
    
    /**
     * 모든 가용 응급실 조회 (거리 제한 없음)
     */
    @Query("SELECT e FROM Emergency e WHERE e.state IN (1, 2)")
    List<Emergency> findAllAvailable();

	List<Emergency> findTop3ByAddressContainingIgnoreCase(String location);
}
