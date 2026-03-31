package fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.session;

import java.util.List;

public record SessionResultResponse(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        List<Integer> values
) {}
