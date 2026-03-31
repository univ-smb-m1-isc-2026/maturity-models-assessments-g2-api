package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.team;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.InvitationStatus;

import java.time.LocalDateTime;

public record InvitationResponse(
        Long id,
        String email,
        String token,
        String teamName,
        String invitedByEmail,
        InvitationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {}


