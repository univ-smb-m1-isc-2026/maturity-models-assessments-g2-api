package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.*;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.InvitationRepository;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final TeamRepository teamRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Create and send an invitation to an email address to join a team.
     * Can only be called by a TEAM_LEAD who owns the target team.
     * @param email the recipient's email address
     * @param teamId the id of the team to join
     * @param invitedBy the authenticated TEAM_LEAD sending the invitation
     * @return the saved Invitation entity
     */
    public Invitation createInvitation(String email, Long teamId, User invitedBy) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Équipe introuvable"));

        if (!team.getLead().getId().equals(invitedBy.getId()))
            throw new RuntimeException("Vous n'êtes pas le lead de cette équipe");

        if (invitationRepository.existsByEmailAndTeam(email, team))
            throw new RuntimeException("Une invitation a déjà été envoyée à cet email pour cette équipe");

        Invitation invitation = Invitation.builder()
                .email(email)
                .token(UUID.randomUUID().toString())
                .team(team)
                .invitedBy(invitedBy)
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        Invitation saved = invitationRepository.save(invitation);

        sendInvitationEmail(saved.getEmail(), saved.getToken());

        return saved;
    }

    /**
     * Validate an invitation token before displaying the registration form.
     * Marks the invitation as EXPIRED if the token has passed its expiry date.
     * @param token the UUID token from the invitation link
     * @return the valid Invitation entity
     */
    public Invitation validateToken(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (invitation.getStatus() != InvitationStatus.PENDING)
            throw new RuntimeException("Invitation déjà utilisée");

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new RuntimeException("Invitation expirée");
        }

        return invitation;
    }

    /**
     * Mark an invitation as ACCEPTED after a successful registration.
     * Should be called by AuthService once the new user has been saved.
     * @param token the UUID token from the invitation link
     */
    public void consumeInvitation(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
        log.info("Invitation acceptée pour {}", invitation.getEmail());
    }

    /**
     * Retrieve an invitation by its token without any validation.
     * Used by AuthService to extract the email and team before registration.
     * @param token the UUID token from the invitation link
     * @return an Optional containing the Invitation if found
     */
    public Optional<Invitation> findByToken(String token) {
        return invitationRepository.findByToken(token);
    }

    /**
     * Send an invitation email via Gmail SMTP with a registration link.
     * @param to the recipient's email address
     * @param token the UUID invitation token
     */
    private void sendInvitationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(to);
        message.setSubject("Invitation à rejoindre une équipe");
        message.setText(
                "Vous avez été invité à rejoindre une équipe sur Maturity Models App.\n\n"
                        + "Cliquez sur le lien suivant pour créer votre compte :\n"
                        + frontendUrl + "/register?token=" + token + "\n\n"
                        + "Ce lien expire dans 7 jours."
        );
        mailSender.send(message);
        log.info("Invitation email sent to {}", to);
    }
}