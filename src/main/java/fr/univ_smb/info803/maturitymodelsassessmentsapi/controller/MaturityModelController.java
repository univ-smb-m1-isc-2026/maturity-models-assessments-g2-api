package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.*;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.MaturityModel;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Question;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.QuestionAnswer;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.User;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.MaturityModelService;
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
public class MaturityModelController {

    private final MaturityModelService maturityModelService;

    /**
     * Create - Add a new maturity model
     * @param request an object MaturityModel
     * @return The model object saved
     */
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

    /**
     * Read - Get one model
     * @param id is the id of the model
     * @return A MaturityModel object fulfilled
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaturityModelResponse> getModel(@PathVariable final long id) {
        return maturityModelService.getMaturityModel(id)
                .map(m -> ResponseEntity.ok(toResponse(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Read - Get all models
     * @return - A List object of MaturityModel fulfilled
     */
    @GetMapping
    public ResponseEntity<List<MaturityModelResponse>> getModels() {
        return ResponseEntity.ok(
                maturityModelService.getMaturityModels().stream().map(this::toResponse).toList()
        );
    }

    /**
     * Update - Update an existing model
     * @param id - The id of the model to update
     * @param request - The model object updated
     * @return The updated model
     */
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

    /**
     * Delete - Delete a model
     * @param id - The id of the model to delete
     */
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
