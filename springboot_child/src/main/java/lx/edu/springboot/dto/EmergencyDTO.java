package lx.edu.springboot.dto;

import lx.edu.springboot.entity.Emergency;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmergencyDTO {
    private Long id;
    private String name;
    private String nickname;
    private Double x;
    private Double y;
    private Integer state;
    private String message;
    private String address;
    private String bed_avail;
    private String bed_total;
    
    private Double distance;

    // ✅ Entity → DTO 변환용 정적 메서드
    public static EmergencyDTO from(Emergency e, double distance) {
        EmergencyDTO dto = new EmergencyDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setNickname(e.getNickname());
        dto.setX(e.getX());
        dto.setY(e.getY());
        dto.setState(e.getState());
        dto.setMessage(e.getMessage());
        dto.setAddress(e.getAddress());
        dto.setBed_avail(e.getBed_avail());
        dto.setBed_total(e.getBed_total());
        dto.setDistance(distance);
        return dto;
    }
}
