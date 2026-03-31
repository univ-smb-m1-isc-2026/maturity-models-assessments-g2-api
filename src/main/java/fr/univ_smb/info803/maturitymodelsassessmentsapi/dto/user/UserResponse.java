package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.user;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Role;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role)
{}
