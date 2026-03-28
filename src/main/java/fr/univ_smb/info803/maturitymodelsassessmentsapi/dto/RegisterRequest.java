package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Role;

public record RegisterRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        Role role
) {}
