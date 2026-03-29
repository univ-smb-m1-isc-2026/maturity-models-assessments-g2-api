package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.InvitationRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.TeamRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.TeamResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.UserResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Team;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.User;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.InvitationService;
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
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;
    private final InvitationService invitationService;

    /**
     * Create a new team. Only a TEAM_LEAD can create a team.
     * The authenticated user is automatically set as the team lead.
     */
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

    /**
     * Get all teams. Accessible by all authenticated users.
     */
    @GetMapping
    public ResponseEntity<List<TeamResponse>> getTeams() {
        return ResponseEntity.ok(
                teamService.getTeams().stream().map(this::toResponse).toList()
        );
    }

    /**
     * Get a team by its id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeam(@PathVariable final long id) {
        return teamService.getTeam(id)
                .map(t -> ResponseEntity.ok(toResponse(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all teams led by the authenticated TEAM_LEAD.
     */
    @GetMapping("/my-teams")
    @PreAuthorize("hasRole('TEAM_LEAD')")
    public ResponseEntity<List<TeamResponse>> getMyTeams(
            @AuthenticationPrincipal UserDetails userDetails) {
        User lead = (User) userDetails;
        return ResponseEntity.ok(
                teamService.getByLeadId(lead.getId()).stream().map(this::toResponse).toList()
        );
    }

    /**
     * Get all teams the authenticated TEAM_MEMBER belongs to.
     */
    @GetMapping("/my-memberships")
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ResponseEntity<List<TeamResponse>> getMyMemberships(
            @AuthenticationPrincipal UserDetails userDetails) {
        User member = (User) userDetails;
        return ResponseEntity.ok(
                teamService.getByMembersId(member.getId()).stream().map(this::toResponse).toList()
        );
    }

    /**
     * Update an existing team. Only the lead of the team can update it.
     */
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

    /**
     * Delete a team. Only the lead of the team can delete it.
     */
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

    /**
     * Send an invitation to a user to join the team.
     * Only the lead of the team can invite members.
     */
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
