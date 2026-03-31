package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.model;

import java.util.List;

public record QuestionResponse(
        Long id,
        String text,
        Integer questionOrder,
        List<QuestionAnswerResponse> answers
) {}