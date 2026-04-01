package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.SessionStatus;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Session;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.SessionRepository;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.SessionResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionResultRepository sessionResultRepository;

    public List<Session> getByTeamId(Long teamId){ return sessionRepository.findByTeamId(teamId); }

    public List<Session> getByModelId(Long modelId){ return sessionRepository.findByModelId(modelId); }

    public List<Session> getByStatus(SessionStatus status){ return sessionRepository.findByStatus(status); }

    public Optional<Session> getSession(final Long id){ return sessionRepository.findById(id); }

    public List<Session> getSessions(){ return sessionRepository.findAll(); }

    public Session saveSession(Session session){ return sessionRepository.save(session); }

    @Transactional
    public void deleteSession(final Long id){
        sessionResultRepository.deleteBySessionId(id);
        sessionRepository.deleteById(id);
    }

}
