package fr.univ_smb.info803.maturitymodelsassessmentsapi.repository;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


//JpaRepository fournit toutes les opérations CRUD de base
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
