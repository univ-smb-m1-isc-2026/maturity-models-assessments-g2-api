package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.*;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.InvitationRepository;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock private InvitationRepository invitationRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private InvitationService invitationService;

    private User buildLead(Long id) {
        return User.builder().id(id).email("lead@test.com").role(Role.TEAM_LEAD).status(Status.ACTIF).build();
    }

    private Team buildTeam(Long id, User lead) {
        return Team.builder().id(id).name("Alpha").lead(lead).members(new ArrayList<>()).build();
    }

    // --- validateToken ---

    @Test
    void validateToken_validToken_returnsInvitation() {
        Invitation invitation = Invitation.builder()
                .id(1L).email("new@test.com").token("valid-token")
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(3)) // pas expiré
                .build();
        when(invitationRepository.findByToken("valid-token")).thenReturn(Optional.of(invitation));

        Invitation result = invitationService.validateToken("valid-token");

        assertThat(result.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void validateToken_unknownToken_throwsException() {
        when(invitationRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.validateToken("bad-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Token invalide");
    }

    @Test
    void validateToken_alreadyAccepted_throwsException() {
        Invitation invitation = Invitation.builder()
                .id(1L).token("used-token").status(InvitationStatus.ACCEPTED)
                .expiresAt(LocalDateTime.now().plusDays(3))
                .build();
        when(invitationRepository.findByToken("used-token")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.validateToken("used-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invitation déjà utilisée");
    }

    @Test
    void validateToken_expired_marksAsExpiredAndThrows() {
        Invitation invitation = Invitation.builder()
                .id(1L).token("expired-token").status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().minusDays(1)) // expiré
                .build();
        when(invitationRepository.findByToken("expired-token")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.validateToken("expired-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invitation expirée");

        // Vérifie que le statut a été mis à jour en EXPIRED
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.EXPIRED);
        verify(invitationRepository).save(invitation);
    }

    // --- consumeInvitation ---

    @Test
    void consumeInvitation_marksAsAccepted() {
        Invitation invitation = Invitation.builder()
                .id(1L).email("test@test.com").token("token-abc")
                .status(InvitationStatus.PENDING)
                .build();
        when(invitationRepository.findByToken("token-abc")).thenReturn(Optional.of(invitation));

        invitationService.consumeInvitation("token-abc");

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        verify(invitationRepository).save(invitation);
    }

    @Test
    void consumeInvitation_unknownToken_throwsException() {
        when(invitationRepository.findByToken("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.consumeInvitation("nope"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Token invalide");
    }

    // --- findByToken ---

    @Test
    void findByToken_returnsOptional() {
        Invitation invitation = Invitation.builder().id(1L).token("tok").build();
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));

        assertThat(invitationService.findByToken("tok")).isPresent();
    }

    // --- getInvitationsByTeam ---

    @Test
    void getInvitationsByTeam_returnsList() {
        User lead = buildLead(1L);
        Team team = buildTeam(1L, lead);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(invitationRepository.findByTeam(team)).thenReturn(List.of(
                Invitation.builder().id(1L).email("a@test.com").build()
        ));

        List<Invitation> result = invitationService.getInvitationsByTeam(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getInvitationsByTeam_unknownTeam_throwsException() {
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.getInvitationsByTeam(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Équipe introuvable");
    }

    // --- createInvitation ---

    @Test
    void createInvitation_notLead_throwsException() {
        User lead = buildLead(1L);
        User otherLead = buildLead(99L);
        Team team = buildTeam(1L, lead);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        // otherLead (id=99) essaie d'inviter dans l'équipe du lead (id=1)
        assertThatThrownBy(() -> invitationService.createInvitation("new@test.com", 1L, otherLead))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Vous n'êtes pas le lead de cette équipe");
    }

    @Test
    void createInvitation_duplicateEmail_throwsException() {
        User lead = buildLead(1L);
        Team team = buildTeam(1L, lead);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(invitationRepository.existsByEmailAndTeam("existing@test.com", team)).thenReturn(true);

        assertThatThrownBy(() -> invitationService.createInvitation("existing@test.com", 1L, lead))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Une invitation a déjà été envoyée à cet email pour cette équipe");
    }
}
