package fr.univ_smb.info803.maturitymodelsassessmentsapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "lead_id") // FK
    private User lead;

    @ManyToMany
    @JoinTable(
            name = "team_members",                             // nom de la table de jointure créée en base
            joinColumns = @JoinColumn(name = "team_id"),       // FK vers la table Team
            inverseJoinColumns = @JoinColumn(name = "user_id") // FK vers la table User
    )
    @Builder.Default
    private List<User> members = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
}
