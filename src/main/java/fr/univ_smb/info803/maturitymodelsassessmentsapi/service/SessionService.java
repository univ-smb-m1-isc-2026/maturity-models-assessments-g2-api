package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.enums.SessionStatus;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Session;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public List<Session> getByTeamId(Long teamId){ return sessionRepository.findByTeamId(teamId); }

    public List<Session> getByModelId(Long modelId){ return sessionRepository.findByModelId(modelId); }

    public List<Session> getByStatus(SessionStatus status){ return sessionRepository.findByStatus(status); }

    public Optional<Session> getSession(final String id){ return sessionRepository.findById(id); }

    public List<Session> getSessions(){ return sessionRepository.findAll(); }

    public void deleteSession(final String id){ sessionRepository.deleteById(id); }

    public Session saveSession(Session session){ return sessionRepository.save(session); }

}
