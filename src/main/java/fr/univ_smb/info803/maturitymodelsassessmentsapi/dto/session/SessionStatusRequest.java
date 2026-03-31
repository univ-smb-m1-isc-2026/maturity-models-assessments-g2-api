package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.SessionStatus;

public record SessionStatusRequest(
        SessionStatus status
) {}
