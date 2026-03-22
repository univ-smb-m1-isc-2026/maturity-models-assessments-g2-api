package fr.univ_smb.info803.maturitymodelsassessmentsapi.model;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Session {

    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne
    @JoinColumn(name = "model_id")
    private MaturityModel model;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    private String name;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    private LocalDateTime deadline;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
