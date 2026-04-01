package fr.univ_smb.info803.maturitymodelsassessmentsapi.repository;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.SessionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionResultRepository extends JpaRepository<SessionResult, Long> {
    List<SessionResult> findBySessionId(Long sessionId);
    Optional<SessionResult> findBySessionIdAndUserId(Long sessionId, Long userId);
    void deleteBySessionId(Long sessionId);
}
