package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.SessionStatusRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.session.SessionRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.session.SessionResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.*;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.MaturityModelService;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.SessionService;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.TeamService;
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
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
@Tag(name = "Sessions", description = "Gestion des sessions d'évaluation")
@SecurityRequirement(name = "bearerAuth")
public class SessionController {

    private final SessionService sessionService;
    private final MaturityModelService maturityModelService;
    private final TeamService teamService;

    @Operation(summary = "Créer une session", description = "Crée une session d'évaluation liée à un modèle et une équipe. Statut initial : PENDING. Rôle requis : TEAM_LEAD.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session créée"),
            @ApiResponse(responseCode = "404", description = "Modèle ou équipe introuvable"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PostMapping
    @PreAuthorize("hasRole('TEAM_LEAD')")
    public ResponseEntity<SessionResponse> createSession(
            @RequestBody SessionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User lead = (User) userDetails;

        Optional<MaturityModel> optionalModel = maturityModelService.getMaturityModel(request.modelId());
        if (optionalModel.isEmpty()) {
            log.warn("Model id={} not found", request.modelId());
            return ResponseEntity.notFound().build();
        }

        Optional<Team> optionalTeam = teamService.getTeam(request.teamId());
        if (optionalTeam.isEmpty()) {
            log.warn("Team id={} not found", request.teamId());
            return ResponseEntity.notFound().build();
        }

        Session session = Session.builder()
                .name(request.name())
                .model(optionalModel.get())
                .team(optionalTeam.get())
                .status(SessionStatus.PENDING)
                .deadline(request.deadline())
                .build();

        Session saved = sessionService.saveSession(session);
        log.info("Session '{}' created with id={} by {}", saved.getName(), saved.getId(), lead.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @Operation(summary = "Lister toutes les sessions", description = "Rôles requis : PMO ou TEAM_LEAD.")
    @ApiResponse(responseCode = "200", description = "Liste des sessions")
    @GetMapping
    @PreAuthorize("hasAnyRole('PMO', 'TEAM_LEAD')")
    public ResponseEntity<List<SessionResponse>> getSessions() {
        return ResponseEntity.ok(
                sessionService.getSessions().stream().map(this::toResponse).toList()
        );
    }

    @Operation(summary = "Obtenir une session par id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session trouvée"),
            @ApiResponse(responseCode = "404", description = "Session introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable final Long id) {
        return sessionService.getSession(id)
                .map(s -> ResponseEntity.ok(toResponse(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Sessions par équipe", description = "Retourne toutes les sessions d'une équipe. Rôles requis : PMO ou TEAM_LEAD.")
    @ApiResponse(responseCode = "200", description = "Liste des sessions de l'équipe")
    @GetMapping("/by-team/{teamId}")
    @PreAuthorize("hasAnyRole('PMO', 'TEAM_LEAD')")
    public ResponseEntity<List<SessionResponse>> getSessionsByTeam(@PathVariable final Long teamId) {
        return ResponseEntity.ok(
                sessionService.getByTeamId(teamId).stream().map(this::toResponse).toList()
        );
    }

    @Operation(summary = "Sessions par modèle", description = "Retourne toutes les sessions utilisant un modèle donné. Rôles requis : PMO ou TEAM_LEAD.")
    @ApiResponse(responseCode = "200", description = "Liste des sessions du modèle")
    @GetMapping("/by-model/{modelId}")
    @PreAuthorize("hasAnyRole('PMO', 'TEAM_LEAD')")
    public ResponseEntity<List<SessionResponse>> getSessionsByModel(@PathVariable final Long modelId) {
        return ResponseEntity.ok(
                sessionService.getByModelId(modelId).stream().map(this::toResponse).toList()
        );
    }

    @Operation(summary = "Supprimer une session", description = "Rôle requis : TEAM_LEAD propriétaire de l'équipe liée à la session.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Session supprimée"),
            @ApiResponse(responseCode = "403", description = "Non propriétaire de l'équipe"),
            @ApiResponse(responseCode = "404", description = "Session introuvable")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEAM_LEAD')")
    public ResponseEntity<Void> deleteSession(
            @PathVariable final Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User lead = (User) userDetails;

        Optional<Session> optionalSession = sessionService.getSession(id);
        if (optionalSession.isEmpty()) {
            log.warn("Session id={} not found", id);
            return ResponseEntity.notFound().build();
        }

        Session session = optionalSession.get();

        if (!session.getTeam().getLead().getId().equals(lead.getId())) {
            log.warn("User {} tried to delete session id={} without being its team lead", lead.getEmail(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        sessionService.deleteSession(id);
        log.info("Session id={} deleted by {}", id, lead.getEmail());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Changer le statut d'une session", description = "Permet de passer une session de PENDING à OPEN (les membres peuvent répondre) ou de OPEN à CLOSED. Rôle requis : TEAM_LEAD propriétaire de l'équipe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statut mis à jour"),
            @ApiResponse(responseCode = "403", description = "Non propriétaire de l'équipe"),
            @ApiResponse(responseCode = "404", description = "Session introuvable")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('TEAM_LEAD')")
    public ResponseEntity<SessionResponse> updateStatus(
            @PathVariable final Long id,
            @RequestBody SessionStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User lead = (User) userDetails;

        Optional<Session> optionalSession = sessionService.getSession(id);
        if (optionalSession.isEmpty()) {
            log.warn("Session id={} not found", id);
            return ResponseEntity.notFound().build();
        }

        Session session = optionalSession.get();

        if (!session.getTeam().getLead().getId().equals(lead.getId())) {
            log.warn("User {} tried to update status of session id={} without being its team lead", lead.getEmail(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        session.setStatus(request.status());
        Session saved = sessionService.saveSession(session);
        log.info("Session id={} status changed to {} by {}", id, request.status(), lead.getEmail());
        return ResponseEntity.ok(toResponse(saved));
    }

    /*##########################################################################################*/

    private SessionResponse toResponse(Session s) {
        return new SessionResponse(
                s.getId(),
                s.getName(),
                s.getStatus(),
                s.getModel().getId(),
                s.getTeam().getId(),
                s.getDeadline(),
                s.getCreatedAt()
        );
    }
}