package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto;

public record QuestionAnswerResponse(
        Long id,
        String value,
        int answerOrder
)
{ }
