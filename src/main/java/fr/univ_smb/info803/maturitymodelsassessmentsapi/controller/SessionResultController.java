package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.session.SessionResultRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.session.SessionResultResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.*;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.SessionResultService;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.SessionService;
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
@RequestMapping("/api/sessions/{sessionId}/results")
@Tag(name = "Session Results", description = "Soumission et consultation des résultats d'évaluation")
@SecurityRequirement(name = "bearerAuth")
public class SessionResultController {

    private final SessionResultService sessionResultService;
    private final SessionService sessionService;

    @Operation(summary = "Soumettre ses réponses", description = "Le member soumet une valeur (1-5) par question du modèle, dans l'ordre. La session doit être OPEN. Une seule soumission par member par session. Rôle requis : TEAM_MEMBER.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Réponses enregistrées"),
            @ApiResponse(responseCode = "403", description = "Session non ouverte ou accès refusé"),
            @ApiResponse(responseCode = "404", description = "Session introuvable"),
            @ApiResponse(responseCode = "409", description = "Le member a déjà soumis ses réponses pour cette session")
    })
    @PostMapping
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<SessionResultResponse> submitResult(
            @PathVariable Long sessionId,
            @RequestBody SessionResultRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User member = (User) userDetails;

        Optional<Session> optionalSession = sessionService.getSession(sessionId);
        if (optionalSession.isEmpty()) {
            log.warn("Session id={} not found", sessionId);
            return ResponseEntity.notFound().build();
        }

        Session session = optionalSession.get();

        if (session.getStatus() != SessionStatus.OPEN) {
            log.warn("User {} tried to submit on session id={} with status={}", member.getEmail(), sessionId, session.getStatus());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (sessionResultService.getBySessionIdAndUserId(sessionId, member.getId()).isPresent()) {
            log.warn("User {} already submitted for session id={}", member.getEmail(), sessionId);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        SessionResult result = SessionResult.builder()
                .session(session)
                .user(member)
                .values(request.values())
                .build();

        SessionResult saved = sessionResultService.saveSessionResult(result);
        log.info("User {} submitted result id={} for session id={}", member.getEmail(), saved.getId(), sessionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @Operation(summary = "Tous les résultats d'une session", description = "Retourne les réponses de tous les membres pour construire le diagramme radar. Chaque entrée contient les valeurs par question et l'identité du membre. Rôles requis : TEAM_MEMBER, TEAM_LEAD ou PMO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des résultats"),
            @ApiResponse(responseCode = "404", description = "Session introuvable")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEAD', 'PMO')")
    public ResponseEntity<List<SessionResultResponse>> getResults(@PathVariable Long sessionId) {

        if (sessionService.getSession(sessionId).isEmpty()) {
            log.warn("Session id={} not found", sessionId);
            return ResponseEntity.notFound().build();
        }

        List<SessionResultResponse> results = sessionResultService
                .getBySessionId(sessionId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Ma soumission", description = "Retourne la soumission du member connecté pour la session. Rôle requis : TEAM_MEMBER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Soumission trouvée"),
            @ApiResponse(responseCode = "404", description = "Aucune soumission pour cette session")
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<SessionResultResponse> getMyResult(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User member = (User) userDetails;

        return sessionResultService
                .getBySessionIdAndUserId(sessionId, member.getId())
                .map(r -> ResponseEntity.ok(toResponse(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    /*##########################################################################################*/

    private SessionResultResponse toResponse(SessionResult r) {
        return new SessionResultResponse(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getFirstName(),
                r.getUser().getLastName(),
                r.getValues()
        );
    }
}