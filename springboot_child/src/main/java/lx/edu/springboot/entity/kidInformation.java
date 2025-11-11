package lx.edu.springboot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kidInformation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class kidInformation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String kid;
    private String parentUid;
    private String allergy;
    private int birthYear;
    private String bloodType;
    private String gender;
    private int heightCm;
    private int weightKg;
    private String medicalHistory;
    private String medication;
}
