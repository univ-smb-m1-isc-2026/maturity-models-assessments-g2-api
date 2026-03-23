package fr.univ_smb.info803.maturitymodelsassessmentsapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "session_results")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionResult {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // une valeur par question
    @ElementCollection // Ce n'est pas une entité, mais une collection de valeurs simples
    @CollectionTable(name = "result_values",                // nom de la table créée
            joinColumns = @JoinColumn(name = "result_id"))  // FK vers session_results
    @Column(name = "value") // nom de la colonne qui stocke chaque entier
    private List<Integer> values = new ArrayList<>();
}
