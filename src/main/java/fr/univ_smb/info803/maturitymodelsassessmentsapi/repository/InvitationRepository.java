package fr.univ_smb.info803.maturitymodelsassessmentsapi.repository;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Invitation;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);
    List<Invitation> findByTeam(Team team);
    boolean existsByEmailAndTeam(String email, Team team);
}