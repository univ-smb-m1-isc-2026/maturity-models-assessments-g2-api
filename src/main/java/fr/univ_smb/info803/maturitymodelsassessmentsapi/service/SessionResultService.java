package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.SessionResult;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.SessionResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionResultService {

    private final SessionResultRepository sessionResultRepository;

    public List<SessionResult> getBySessionId(Long sessionId){ return sessionResultRepository.findBySessionId(sessionId); }

    public Optional<SessionResult> getBySessionIdAndUserId(Long sessionId, Long userId){ return sessionResultRepository.findBySessionIdAndUserId(sessionId, userId); }

    public Optional<SessionResult> getSessionResult(final Long id){ return sessionResultRepository.findById(id); }

    public List<SessionResult> getSessionResults(){ return sessionResultRepository.findAll(); }

    public void deleteSessionResult(final Long id){ sessionResultRepository.deleteById(id); }

    public SessionResult saveSessionResult(SessionResult sessionResult){ return sessionResultRepository.save(sessionResult); }
}
