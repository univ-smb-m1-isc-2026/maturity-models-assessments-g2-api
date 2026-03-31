package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.model;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.MaturityCategory;

import java.util.List;

public record MaturityModelRequest(
        String title,
        String description,
        MaturityCategory category,
        String icon,
        List<QuestionRequest> questions
) {}