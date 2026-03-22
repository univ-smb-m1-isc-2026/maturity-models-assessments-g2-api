package fr.univ_smb.info803.maturitymodelsassessmentsapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(name = "question_order")
    private Integer questionOrder;

    @ManyToOne
    @JoinColumn(name = "model_id")
    @JsonIgnore
    private MaturityModel model;
}
