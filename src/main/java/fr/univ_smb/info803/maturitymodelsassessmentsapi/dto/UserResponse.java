package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.enums.Role;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role)
{}
