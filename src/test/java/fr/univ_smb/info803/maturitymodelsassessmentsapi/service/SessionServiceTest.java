package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Session;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.SessionStatus;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.SessionRepository;
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
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionResultRepository sessionResultRepository;

    @InjectMocks
    private SessionService sessionService;

    private Session buildSession(Long id, String name, SessionStatus status) {
        return Session.builder().id(id).name(name).status(status).build();
    }

    @Test
    void getSession_existingId_returnsSession() {
        Session session = buildSession(1L, "Sprint Review", SessionStatus.OPEN);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Optional<Session> result = sessionService.getSession(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Sprint Review");
    }

    @Test
    void getSession_unknownId_returnsEmpty() {
        when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThat(sessionService.getSession(999L)).isEmpty();
    }

    @Test
    void getSessions_returnsAll() {
        when(sessionRepository.findAll()).thenReturn(List.of(
                buildSession(1L, "A", SessionStatus.OPEN),
                buildSession(2L, "B", SessionStatus.CLOSED)
        ));

        assertThat(sessionService.getSessions()).hasSize(2);
    }

    @Test
    void getByTeamId_returnsList() {
        when(sessionRepository.findByTeamId(1L)).thenReturn(List.of(buildSession(1L, "A", SessionStatus.OPEN)));

        assertThat(sessionService.getByTeamId(1L)).hasSize(1);
    }

    @Test
    void getByModelId_returnsList() {
        when(sessionRepository.findByModelId(1L)).thenReturn(List.of(buildSession(1L, "A", SessionStatus.OPEN)));

        assertThat(sessionService.getByModelId(1L)).hasSize(1);
    }

    @Test
    void getByStatus_returnsList() {
        when(sessionRepository.findByStatus(SessionStatus.PENDING))
                .thenReturn(List.of(buildSession(1L, "A", SessionStatus.PENDING)));

        assertThat(sessionService.getByStatus(SessionStatus.PENDING)).hasSize(1);
    }

    @Test
    void saveSession_callsRepository() {
        Session session = buildSession(null, "New", SessionStatus.PENDING);
        Session saved = buildSession(1L, "New", SessionStatus.PENDING);
        when(sessionRepository.save(session)).thenReturn(saved);

        Session result = sessionService.saveSession(session);

        assertThat(result.getId()).isEqualTo(1L);
        verify(sessionRepository).save(session);
    }

    @Test
    void deleteSession_deletesResultsThenSession() {
        // deleteSession doit d'abord supprimer les résultats, puis la session
        sessionService.deleteSession(1L);

        // On vérifie l'ordre : d'abord les résultats, puis la session
        var inOrder = inOrder(sessionResultRepository, sessionRepository);
        inOrder.verify(sessionResultRepository).deleteBySessionId(1L);
        inOrder.verify(sessionRepository).deleteById(1L);
    }
}
