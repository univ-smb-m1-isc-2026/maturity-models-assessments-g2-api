package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.model;

public record QuestionAnswerRequest(
        String value,
        int answerOrder
) {}