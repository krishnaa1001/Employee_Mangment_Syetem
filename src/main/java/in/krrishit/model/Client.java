package in.krrishit.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Unique Client ID (e.g., krrishitCli001)
    @Column(unique = true)
    private String clientId;
    
    private String companyName;
    private String projectName;
    
    @Column(unique = true)
    private String email;
    
    private String country;
    private String city;
    private String address;
    
    private LocalDate startDate;
    private LocalDate deadlineDate;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
