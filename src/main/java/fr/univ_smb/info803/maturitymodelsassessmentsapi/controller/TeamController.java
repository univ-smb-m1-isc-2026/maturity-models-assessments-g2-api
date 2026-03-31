package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.team.InvitationRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.team.InvitationResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.team.TeamRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.team.TeamResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.user.UserResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.InvitationStatus;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Team;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.User;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.InvitationService;
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
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Gestion des équipes et des invitations")
@SecurityRequirement(name = "bearerAuth")
public class TeamController {

    private final TeamService teamService;
    private final InvitationService invitationService;

    @Operation(summary = "Créer une équipe", description = "Crée une nouvelle équipe. Le lead est automatiquement l'utilisateur connecté. Rôle requis : TEAM_LEAD.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Équipe créée"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PostMapping
    @PreAuthorize("hasRole('TEAM_LEAD')")
    public ResponseEntity<TeamResponse> createTeam(
            @RequestBody TeamRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User lead = (User) userDetails;

        Team team = Team.builder()
                .name(request.name())
                .lead(lead)
                .build();

        log.info("Team '{}' created by {}", team.getName(), lead.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(teamService.saveTeam(team)));
    }

    @Operation(summary = "Lister toutes les équipes", description = "Retourne toutes les équipes. Accessible à tous les utilisateurs authentifiés.")
    @ApiResponse(responseCode = "200", description = "Liste des équipes")
    @GetMapping
    public ResponseEntity<List<TeamResponse>> getTeams() {
        return ResponseEntity.ok(
                teamService.getTeams().stream().map(this::toResponse).toList()
        );
    }

    @Operation(summary = "Obtenir une équipe par id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Équipe trouvée"),
            @ApiResponse(responseCode = "404", description = "Équipe introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeam(@PathVariable final long id) {
        return teamService.getTeam(id)
                .map(t -> ResponseEntity.ok(toResponse(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Mes équipes", description = "Retourne les équipes dont le lead connecté est responsable. Rôle requis : TEAM_LEAD.")
    @ApiResponse(responseCode = "200", description = "Liste des équipes du lead connecté")
    @GetMapping("/my-teams")
    @PreAuthorize("hasRole('TEAM_LEAD')")
    public ResponseEntity<List<TeamResponse>> getMyTeams(
            @AuthenticationPrincipal UserDetails userDetails) {
        User lead = (User) userDetails;
        return ResponseEntity.ok(
                teamService.getByLeadId(lead.getId()).stream().map(this::toResponse).toList()
        );
    }

    @Operation(summary = "Mes memberships", description = "Retourne les équipes dont le member connecté fait partie. Rôle requis : TEAM_MEMBER.")
    @ApiResponse(responseCode = "200", description = "Liste des équipes du member connecté")
    @GetMapping("/my-memberships")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<List<TeamResponse>> getMyMemberships(
            @AuthenticationPrincipal UserDetails userDetails) {
        User member = (User) userDetails;
        return ResponseEntity.ok(
                teamService.getByMembersId(member.getId()).stream().map(this::toResponse).toList()
        );
    }

    @Operation(summary = "Modifier une équipe", description = "Met à jour le nom d'une équipe. Rôle requis : TEAM_LEAD propriétaire de l'équipe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Équipe mise à jour"),
            @ApiResponse(responseCode = "403", description = "Non propriétaire de l'équipe"),
            @ApiResponse(responseCode = "404", description = "Équipe introuvable")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEAM_LEAD')")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable final Long id,
            @RequestBody TeamRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User lead = (User) userDetails;

        Optional<Team> optionalTeam = teamService.getTeam(id);

        if (optionalTeam.isEmpty()) {
            log.warn("Team id={} not found", id);
            return ResponseEntity.notFound().build();
        }

        Team team = optionalTeam.get();

        if (!team.getLead().getId().equals(lead.getId())) {
            log.warn("User {} tried to update team {} without being its lead", lead.getEmail(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.name() != null) team.setName(request.name());

        log.info("Team id={} updated by {}", id, lead.getEmail());
        return ResponseEntity.ok(toResponse(teamService.saveTeam(team)));
    }

    @Operation(summary = "Supprimer une équipe", description = "Supprime une équipe. Rôle requis : TEAM_LEAD propriétaire de l'équipe.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Équipe supprimée"),
            @ApiResponse(responseCode = "403", description = "Non propriétaire de l'équipe"),
            @ApiResponse(responseCode = "404", description = "Équipe introuvable")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEAM_LEAD')")
    public ResponseEntity<Void> deleteTeam(
            @PathVariable final Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User lead = (User) userDetails;

        Optional<Team> optionalTeam = teamService.getTeam(id);

        if (optionalTeam.isEmpty()) {
            log.warn("Team id={} not found", id);
            return ResponseEntity.notFound().build();
        }

        Team team = optionalTeam.get();

        if (!team.getLead().getId().equals(lead.getId())) {
            log.warn("User {} tried to delete team {} without being its lead", lead.getEmail(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        teamService.deleteTeam(id);
        log.info("Team id={} deleted by {}", id, lead.getEmail());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Inviter un membre", description = "Envoie un email d'invitation à rejoindre l'équipe. Rôle requis : TEAM_LEAD propriétaire de l'équipe.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invitation envoyée"),
            @ApiResponse(responseCode = "400", description = "Email déjà invité pour cette équipe"),
            @ApiResponse(responseCode = "403", description = "Non propriétaire de l'équipe"),
            @ApiResponse(responseCode = "404", description = "Équipe introuvable")
    })
    @PostMapping("/{id}/invitations")
    @PreAuthorize("hasRole('TEAM_LEAD')")
    public ResponseEntity<Void> inviteMember(
            @PathVariable final Long id,
            @RequestBody InvitationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User lead = (User) userDetails;

        invitationService.createInvitation(request.email(), id, lead);
        log.info("Invitation sent to {} for team id={} by {}", request.email(), id, lead.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Invitations en attente", description = "Retourne les invitations PENDING pour une équipe. Rôle requis : TEAM_LEAD propriétaire de l'équipe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des invitations en attente"),
            @ApiResponse(responseCode = "403", description = "Non propriétaire de l'équipe"),
            @ApiResponse(responseCode = "404", description = "Équipe introuvable")
    })
    @GetMapping("/{id}/invitations")
    @PreAuthorize("hasRole('TEAM_LEAD')")
    public ResponseEntity<List<InvitationResponse>> getTeamInvitations(
            @PathVariable final Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User lead = (User) userDetails;

        Optional<Team> optionalTeam = teamService.getTeam(id);
        if (optionalTeam.isEmpty()) {
            log.warn("Team id={} not found", id);
            return ResponseEntity.notFound().build();
        }

        if (!optionalTeam.get().getLead().getId().equals(lead.getId())) {
            log.warn("User {} tried to access invitations of team id={} without being its lead", lead.getEmail(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<InvitationResponse> invitations = invitationService.getInvitationsByTeam(id)
                .stream()
                .filter(inv -> inv.getStatus() == InvitationStatus.PENDING)
                .map(inv -> new InvitationResponse(
                        inv.getId(),
                        inv.getEmail(),
                        inv.getToken(),
                        inv.getTeam().getName(),
                        inv.getInvitedBy().getEmail(),
                        inv.getStatus(),
                        inv.getExpiresAt(),
                        inv.getCreatedAt()
                ))
                .toList();

        log.info("Invitations fetched for team id={} by {}", id, lead.getEmail());
        return ResponseEntity.ok(invitations);
    }

    /*##########################################################################################*/

    private TeamResponse toResponse(Team t) {
        List<UserResponse> members = t.getMembers().stream()
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.getEmail(),
                        u.getRole()
                ))
                .toList();

        UserResponse lead = t.getLead() == null ? null : new UserResponse(
                t.getLead().getId(),
                t.getLead().getFirstName(),
                t.getLead().getLastName(),
                t.getLead().getEmail(),
                t.getLead().getRole()
        );

        return new TeamResponse(t.getId(), t.getName(), lead, members, t.getCreatedAt());
    }
}