package fr.univ_smb.info803.maturitymodelsassessmentsapi.repository;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.enums.MaturityCategory;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.MaturityModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaturityModelRepository extends JpaRepository<MaturityModel, Long> {
    List<MaturityModel> findByCategory(MaturityCategory category);
    List<MaturityModel> findByCreatedById(Long userId);
}
