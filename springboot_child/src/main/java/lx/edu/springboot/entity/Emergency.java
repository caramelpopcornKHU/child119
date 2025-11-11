package lx.edu.springboot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "emergency")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Emergency {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Transient // DB에 실제 컬럼 없음, JPA가 무시
    private Double distance;
}
