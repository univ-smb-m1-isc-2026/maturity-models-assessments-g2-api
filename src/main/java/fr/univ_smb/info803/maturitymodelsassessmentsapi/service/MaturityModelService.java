package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.enums.MaturityCategory;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.MaturityModel;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.MaturityModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaturityModelService {

    private final MaturityModelRepository maturityModelRepository;

    public List<MaturityModel> findByCategory(MaturityCategory category){ return maturityModelRepository.findByCategory(category); }

    public List<MaturityModel> findByCreatedById(Long modelId){ return maturityModelRepository.findByCreatedById(modelId); }

    public Optional<MaturityModel> getMaturityModel(Long id){ return maturityModelRepository.findById(id); }

    public List<MaturityModel> getMaturityModels(){ return maturityModelRepository.findAll(); }

    public void deleteModel(final Long modelId) { maturityModelRepository.deleteById(modelId); }

    public MaturityModel saveModel(MaturityModel model){ return maturityModelRepository.save(model); }
}
