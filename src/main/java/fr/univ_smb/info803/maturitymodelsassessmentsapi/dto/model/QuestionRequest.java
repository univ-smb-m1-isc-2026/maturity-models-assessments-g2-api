package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.model;

import java.util.List;

public record QuestionRequest(
        String text,
        Integer questionOrder,
        List<QuestionAnswerRequest> answers
) {}
