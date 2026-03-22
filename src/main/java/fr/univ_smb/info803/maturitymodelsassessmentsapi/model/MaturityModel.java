package fr.univ_smb.info803.maturitymodelsassessmentsapi.model;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.enums.MaturityCategory;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.enums.MaturityLevel;
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
@Table(name = "maturity_models")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MaturityModel {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private MaturityCategory category;

    private String icon;

    //Stocké comme tableau PostGreSQL
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "model_level", joinColumns = @JoinColumn(name = "model_id"))
    @Column(name = "level")
    private List<MaturityLevel> levels = new ArrayList<>();

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionOrder ASC")
    private List<Question> questions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
