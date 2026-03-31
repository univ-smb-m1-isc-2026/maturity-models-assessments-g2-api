package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.session;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.SessionStatus;

import java.time.LocalDateTime;

public record SessionResponse(
        Long id,
        String name,
        SessionStatus status,
        Long modelId,
        Long teamId,
        LocalDateTime deadline,
        LocalDateTime createdAt
) { }