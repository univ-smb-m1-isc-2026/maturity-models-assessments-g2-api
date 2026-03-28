package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto;

import java.util.List;

public record QuestionRequest(
        String text,
        Integer questionOrder,
        List<QuestionAnswerRequest> answers
) {}
