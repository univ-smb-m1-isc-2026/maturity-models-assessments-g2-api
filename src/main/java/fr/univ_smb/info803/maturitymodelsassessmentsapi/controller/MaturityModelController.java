package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.MaturityModel;
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
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/models")
public class MaturityModelController {

    private final MaturityModelService maturityModelService;

    /**
     * Create - Add a new maturity model
     * @param model an object MaturityModel
     * @return The model object saved
     */
    @PostMapping
    @PreAuthorize("hasRole('PMO')")
    public ResponseEntity<MaturityModel> createModel(
            @RequestBody MaturityModel model,
            @AuthenticationPrincipal UserDetails userDetails) {
        User creator = (User) userDetails;
        model.setCreatedBy(creator);
        log.info("Modèle '{}' créé par {}", model.getTitle(), creator.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(maturityModelService.saveModel(model));
    }

    /**
     * Read - Get one model
     * @param id is the id of the model
     * @return A MaturityModel object fulfilled
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaturityModel> getModel(@PathVariable final long id){
        Optional<MaturityModel> model = maturityModelService.getMaturityModel(id);
        if (model.isEmpty()) {
            log.warn("Modèle id={} non trouvé", id);
        }
        return model.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Read - Get all models
     * @return - A List object of MaturityModel fulfilled
     */
    @GetMapping
    public List<MaturityModel> getModels() { return maturityModelService.getMaturityModels(); }

    /**
     * Update - Update an existing model
     * @param id - The id of the model to update
     * @param model - The model object updated
     * @return The updated model
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PMO')")
    public ResponseEntity<MaturityModel> updateModel(@PathVariable final Long id, @RequestBody MaturityModel model) {
        Optional<MaturityModel> m = maturityModelService.getMaturityModel(id);
        if(m.isPresent()) {
            MaturityModel currentModel = m.get();

            if(model.getTitle() != null)  currentModel.setTitle(model.getTitle());
            if(model.getDescription() != null)   currentModel.setDescription(model.getDescription());
            if(model.getCategory() != null)      currentModel.setCategory(model.getCategory());
            if(model.getIcon() != null)   currentModel.setIcon(model.getIcon());

            if(model.getQuestions() != null && !model.getQuestions().isEmpty())
                currentModel.setQuestions(model.getQuestions());

            maturityModelService.saveModel(currentModel);
            log.info("Modèle id={} mis à jour", id);
            return ResponseEntity.ok(currentModel);
        }
        log.warn("Tentative de mise à jour du modèle id={} non trouvé", id);
        return ResponseEntity.notFound().build();
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
}
