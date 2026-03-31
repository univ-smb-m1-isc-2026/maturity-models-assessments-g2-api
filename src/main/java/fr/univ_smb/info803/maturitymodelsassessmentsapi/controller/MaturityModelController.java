package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.model.*;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.user.UserResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.MaturityModel;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Question;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.QuestionAnswer;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.User;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.MaturityModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/models")
@Tag(name = "Maturity Models", description = "Gestion des modèles de maturité et de leurs questions")
@SecurityRequirement(name = "bearerAuth")
public class MaturityModelController {

    private final MaturityModelService maturityModelService;

    @Operation(summary = "Créer un modèle", description = "Crée un modèle de maturité avec ses questions et réponses possibles (1 à 5). Rôle requis : PMO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Modèle créé"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PostMapping
    @PreAuthorize("hasRole('PMO')")
    public ResponseEntity<MaturityModelResponse> createModel(
            @RequestBody MaturityModelRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User creator = (User) userDetails;

        MaturityModel model = MaturityModel.builder()
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .icon(request.icon())
                .createdBy(creator)
                .build();

        if (request.questions() != null)
            model.setQuestions(buildQuestions(request.questions(), model));

        log.info("Modèle '{}' créé par {}", model.getTitle(), creator.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(maturityModelService.saveModel(model)));
    }

    @Operation(summary = "Obtenir un modèle par id", description = "Retourne le modèle avec toutes ses questions et réponses possibles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Modèle trouvé"),
            @ApiResponse(responseCode = "404", description = "Modèle introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MaturityModelResponse> getModel(@PathVariable final long id) {
        return maturityModelService.getMaturityModel(id)
                .map(m -> ResponseEntity.ok(toResponse(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lister tous les modèles", description = "Accessible à tous les utilisateurs authentifiés.")
    @ApiResponse(responseCode = "200", description = "Liste des modèles")
    @GetMapping
    public ResponseEntity<List<MaturityModelResponse>> getModels() {
        return ResponseEntity.ok(
                maturityModelService.getMaturityModels().stream().map(this::toResponse).toList()
        );
    }

    @Operation(summary = "Modifier un modèle", description = "Met à jour le titre, la description, la catégorie, l'icône ou les questions. Rôle requis : PMO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Modèle mis à jour"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Modèle introuvable")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PMO')")
    public ResponseEntity<MaturityModelResponse> updateModel(
            @PathVariable final Long id,
            @RequestBody MaturityModelRequest request) {

        return maturityModelService.getMaturityModel(id).map(current -> {
            if (request.title()       != null) current.setTitle(request.title());
            if (request.description() != null) current.setDescription(request.description());
            if (request.category()    != null) current.setCategory(request.category());
            if (request.icon()        != null) current.setIcon(request.icon());

            if (request.questions() != null) {
                current.getQuestions().clear();
                current.getQuestions().addAll(buildQuestions(request.questions(), current));
            }

            log.info("Modèle id={} mis à jour", id);
            return ResponseEntity.ok(toResponse(maturityModelService.saveModel(current)));
        }).orElseGet(() -> {
            log.warn("Modèle id={} non trouvé", id);
            return ResponseEntity.notFound().build();
        });
    }

    @Operation(summary = "Supprimer un modèle", description = "Rôle requis : PMO.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Modèle supprimé"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Modèle introuvable")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PMO')")
    public ResponseEntity<Void> deleteModel(@PathVariable final Long id) {
        if (maturityModelService.getMaturityModel(id).isEmpty()) {
            log.warn("Tentative de suppression du modèle id={} non trouvé", id);
            return ResponseEntity.notFound().build();
        }
        maturityModelService.deleteModel(id);
        log.info("Modèle id={} supprimé", id);
        return ResponseEntity.noContent().build();
    }

    /*##########################################################################################*/

    private List<Question> buildQuestions(List<QuestionRequest> requests, MaturityModel model) {
        return requests.stream()
                .map(q -> {
                    Question question = Question.builder()
                            .text(q.text())
                            .questionOrder(q.questionOrder())
                            .model(model)
                            .build();
                    if (q.answers() != null)
                        question.setAnswers(buildAnswers(q.answers(), question));
                    return question;
                })
                .toList();
    }

    private List<QuestionAnswer> buildAnswers(List<QuestionAnswerRequest> requests, Question question) {
        return requests.stream()
                .map(a -> QuestionAnswer.builder()
                        .value(a.value())
                        .answerOrder(a.answerOrder())
                        .question(question)
                        .build())
                .toList();
    }

    private MaturityModelResponse toResponse(MaturityModel m) {
        UserResponse createdBy = m.getCreatedBy() == null ? null : new UserResponse(
                m.getCreatedBy().getId(),
                m.getCreatedBy().getFirstName(),
                m.getCreatedBy().getLastName(),
                m.getCreatedBy().getEmail(),
                m.getCreatedBy().getRole()
        );

        List<QuestionResponse> questions = m.getQuestions().stream()
                .map(q -> new QuestionResponse(
                        q.getId(),
                        q.getText(),
                        q.getQuestionOrder(),
                        q.getAnswers().stream()
                                .map(a -> new QuestionAnswerResponse(a.getId(), a.getValue(), a.getAnswerOrder()))
                                .toList()
                ))
                .toList();

        return new MaturityModelResponse(
                m.getId(),
                m.getTitle(),
                m.getDescription(),
                m.getCategory(),
                m.getIcon(),
                questions,
                createdBy,
                m.getCreatedAt()
        );
    }
}
