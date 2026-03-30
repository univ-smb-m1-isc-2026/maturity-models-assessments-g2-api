package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.SessionRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.SessionResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.*;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.MaturityModelService;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.SessionService;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.TeamService;
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
public class SessionController {

    private final SessionService sessionService;
    private final MaturityModelService maturityModelService;
    private final TeamService teamService;

    /**
     * Create a new assessment session.
     * Only accessible by users with the TEAM_LEAD role.
     *
     * @param request     the session creation payload (modelId, teamId, name, deadline)
     * @param userDetails the authenticated user, expected to be a {@link User} instance
     * @return 201 Created with the created session,
     *         or 404 Not Found if the model or team does not exist
     */
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

    /**
     * Retrieve all sessions.
     * Accessible by ADMIN and TEAM_LEAD roles.
     *
     * @return 200 OK with the list of all sessions
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD')")
    public ResponseEntity<List<SessionResponse>> getSessions() {
        return ResponseEntity.ok(
                sessionService.getSessions().stream().map(this::toResponse).toList()
        );
    }

    /**
     * Retrieve a single session by its ID.
     *
     * @param id the unique identifier of the session
     * @return 200 OK with the matching session,
     *         or 404 Not Found if no session exists with the given ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD')")
    public ResponseEntity<SessionResponse> getSession(@PathVariable final Long id) {
        return sessionService.getSession(id)
                .map(s -> ResponseEntity.ok(toResponse(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieve all sessions belonging to a specific team.
     *
     * @param teamId the ID of the team
     * @return 200 OK with the list of sessions for the given team
     */
    @GetMapping("/by-team/{teamId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD')")
    public ResponseEntity<List<SessionResponse>> getSessionsByTeam(@PathVariable final Long teamId) {
        return ResponseEntity.ok(
                sessionService.getByTeamId(teamId).stream().map(this::toResponse).toList()
        );
    }

    /**
     * Retrieve all sessions associated with a specific maturity model.
     *
     * @param modelId the ID of the maturity model
     * @return 200 OK with the list of sessions for the given model
     */
    @GetMapping("/by-model/{modelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD')")
    public ResponseEntity<List<SessionResponse>> getSessionsByModel(@PathVariable final Long modelId) {
        return ResponseEntity.ok(
                sessionService.getByModelId(modelId).stream().map(this::toResponse).toList()
        );
    }

    /**
     * Delete a session by its ID.
     * Only the TEAM_LEAD who owns the session's team can delete it.
     *
     * @param id          the unique identifier of the session to delete
     * @param userDetails the authenticated user
     * @return 204 No Content on success,
     *         403 Forbidden if the user is not the team lead,
     *         or 404 Not Found if no session exists with the given ID
     */
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