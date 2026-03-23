package fr.univ_smb.info803.maturitymodelsassessmentsapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.enums.Role;
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
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany(mappedBy = "members")
    @JsonIgnore
    @Builder.Default
    private List<Team> teams = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
}


