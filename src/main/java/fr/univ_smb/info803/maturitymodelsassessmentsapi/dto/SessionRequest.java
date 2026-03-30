package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto;

import java.time.LocalDateTime;

public record SessionRequest(
        Long modelId,
        Long teamId,
        String name,
        LocalDateTime deadline
) {}