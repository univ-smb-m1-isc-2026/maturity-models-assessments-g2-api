package fr.univ_smb.info803.maturitymodelsassessmentsapi.repository;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.SessionStatus;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByTeamId(Long teamId);
    List<Session> findByModelId(Long modelId);
    List<Session> findByStatus(SessionStatus status);
}
