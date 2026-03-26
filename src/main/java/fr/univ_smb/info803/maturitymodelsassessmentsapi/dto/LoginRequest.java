package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto;

public record LoginRequest(
        String email,
        String password
) {}