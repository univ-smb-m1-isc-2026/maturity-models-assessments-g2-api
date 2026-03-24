package fr.univ_smb.info803.maturitymodelsassessmentsapi.repository;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.enums.Role;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRole(Role role);
}
