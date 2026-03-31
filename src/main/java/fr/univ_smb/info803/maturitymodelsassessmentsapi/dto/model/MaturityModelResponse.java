package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.model;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.user.UserResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.MaturityCategory;

import java.time.LocalDateTime;
import java.util.List;

public record MaturityModelResponse(
        Long id,
        String title,
        String description,
        MaturityCategory category,
        String icon,
        List<QuestionResponse> questions,
        UserResponse createdBy,
        LocalDateTime createdAt
) {}
