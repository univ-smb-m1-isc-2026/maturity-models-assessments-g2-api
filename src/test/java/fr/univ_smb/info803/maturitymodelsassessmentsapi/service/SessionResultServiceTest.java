package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.SessionResult;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.SessionResultRepository;
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
class SessionResultServiceTest {

    @Mock
    private SessionResultRepository sessionResultRepository;

    @InjectMocks
    private SessionResultService sessionResultService;

    private SessionResult buildResult(Long id) {
        return SessionResult.builder().id(id).values(List.of(3, 4, 5, 2, 1)).build();
    }

    @Test
    void getSessionResult_existingId_returnsResult() {
        SessionResult result = buildResult(1L);
        when(sessionResultRepository.findById(1L)).thenReturn(Optional.of(result));

        Optional<SessionResult> found = sessionResultService.getSessionResult(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getValues()).containsExactly(3, 4, 5, 2, 1);
    }

    @Test
    void getSessionResult_unknownId_returnsEmpty() {
        when(sessionResultRepository.findById(999L)).thenReturn(Optional.empty());

        assertThat(sessionResultService.getSessionResult(999L)).isEmpty();
    }

    @Test
    void getBySessionId_returnsList() {
        when(sessionResultRepository.findBySessionId(1L)).thenReturn(List.of(buildResult(1L), buildResult(2L)));

        assertThat(sessionResultService.getBySessionId(1L)).hasSize(2);
    }

    @Test
    void getBySessionIdAndUserId_returnsResult() {
        SessionResult result = buildResult(1L);
        when(sessionResultRepository.findBySessionIdAndUserId(1L, 5L)).thenReturn(Optional.of(result));

        Optional<SessionResult> found = sessionResultService.getBySessionIdAndUserId(1L, 5L);

        assertThat(found).isPresent();
    }

    @Test
    void getSessionResults_returnsAll() {
        when(sessionResultRepository.findAll()).thenReturn(List.of(buildResult(1L)));

        assertThat(sessionResultService.getSessionResults()).hasSize(1);
    }

    @Test
    void saveSessionResult_callsRepository() {
        SessionResult result = buildResult(null);
        SessionResult saved = buildResult(1L);
        when(sessionResultRepository.save(result)).thenReturn(saved);

        SessionResult res = sessionResultService.saveSessionResult(result);

        assertThat(res.getId()).isEqualTo(1L);
        verify(sessionResultRepository).save(result);
    }

    @Test
    void deleteSessionResult_callsRepository() {
        sessionResultService.deleteSessionResult(1L);

        verify(sessionResultRepository, times(1)).deleteById(1L);
    }
}
