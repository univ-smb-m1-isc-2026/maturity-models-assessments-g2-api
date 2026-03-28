package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Role;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Status;

public record UserRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        Role role,
        Status status
)
{ }
