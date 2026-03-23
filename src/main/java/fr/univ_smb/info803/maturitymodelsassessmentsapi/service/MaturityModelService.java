package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.enums.MaturityCategory;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.MaturityModel;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.MaturityModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaturityModelService {

    private final MaturityModelRepository maturityModelRepository;

    public List<MaturityModel> findByCategory(MaturityCategory category){ return maturityModelRepository.findByCategory(category); }

    public List<MaturityModel> findByCreatedById(Long userId){ return maturityModelRepository.findByCreatedById(userId); }

    public Optional<MaturityModel> getMaturityModel(Long id){ return maturityModelRepository.findById(id); }

    public List<MaturityModel> getMaturityModels(){ return maturityModelRepository.findAll(); }

}
