package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Team;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    public List<Team> getByLeadId(Long leadId){ return teamRepository.findByLeadId(leadId); }

    public List<Team> getByMembersId(Long userId){ return teamRepository.findByMembersId(userId); }

    public Optional<Team> getTeam(final long id) { return teamRepository.findById(id); }

    public List<Team> getTeams(){
        return teamRepository.findAll();
    }

    public void deleteTeam(final long id){ teamRepository.deleteById(id); }

    public Team saveTeam(Team team){
        return teamRepository.save(team);
    }
}
