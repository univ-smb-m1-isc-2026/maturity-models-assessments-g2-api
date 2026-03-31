package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.auth;

public record LoginRequest(
        String email,
        String password
) {}