package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.team;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.user.UserResponse;

import java.time.LocalDateTime;
import java.util.List;

public record TeamResponse(
        Long id,
        String name,
        UserResponse lead,
        List<UserResponse> members,
        LocalDateTime createdAt
) { }
