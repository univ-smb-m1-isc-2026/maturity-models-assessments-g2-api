package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.MaturityCategory;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.MaturityModel;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.MaturityModelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaturityModelServiceTest {

    @Mock
    private MaturityModelRepository maturityModelRepository;

    @InjectMocks
    private MaturityModelService maturityModelService;

    private MaturityModel buildModel(Long id, String title, MaturityCategory category) {
        return MaturityModel.builder()
                .id(id).title(title).description("desc").category(category).icon("icon")
                .build();
    }

    @Test
    void getMaturityModel_existingId_returnsModel() {
        MaturityModel model = buildModel(1L, "SCRUM Model", MaturityCategory.SCRUM);
        when(maturityModelRepository.findById(1L)).thenReturn(Optional.of(model));

        Optional<MaturityModel> result = maturityModelService.getMaturityModel(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("SCRUM Model");
    }

    @Test
    void getMaturityModel_unknownId_returnsEmpty() {
        when(maturityModelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThat(maturityModelService.getMaturityModel(999L)).isEmpty();
    }

    @Test
    void getMaturityModels_returnsAll() {
        when(maturityModelRepository.findAll()).thenReturn(List.of(
                buildModel(1L, "A", MaturityCategory.SCRUM),
                buildModel(2L, "B", MaturityCategory.DEVOPS)
        ));

        assertThat(maturityModelService.getMaturityModels()).hasSize(2);
    }

    @Test
    void findByCategory_returnsList() {
        when(maturityModelRepository.findByCategory(MaturityCategory.AGILE))
                .thenReturn(List.of(buildModel(1L, "Agile", MaturityCategory.AGILE)));

        assertThat(maturityModelService.findByCategory(MaturityCategory.AGILE)).hasSize(1);
    }

    @Test
    void findByCreatedById_returnsList() {
        when(maturityModelRepository.findByCreatedById(5L))
                .thenReturn(List.of(buildModel(1L, "Custom", MaturityCategory.CUSTOM)));

        assertThat(maturityModelService.findByCreatedById(5L)).hasSize(1);
    }

    @Test
    void saveModel_callsRepository() {
        MaturityModel model = buildModel(null, "New", MaturityCategory.QUALITY);
        MaturityModel saved = buildModel(1L, "New", MaturityCategory.QUALITY);
        when(maturityModelRepository.save(model)).thenReturn(saved);

        MaturityModel result = maturityModelService.saveModel(model);

        assertThat(result.getId()).isEqualTo(1L);
        verify(maturityModelRepository).save(model);
    }

    @Test
    void deleteModel_callsRepository() {
        maturityModelService.deleteModel(1L);

        verify(maturityModelRepository, times(1)).deleteById(1L);
    }
}
