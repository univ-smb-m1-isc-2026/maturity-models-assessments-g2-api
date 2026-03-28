package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto;

public record QuestionAnswerRequest(
        String value,
        int answerOrder
) {}