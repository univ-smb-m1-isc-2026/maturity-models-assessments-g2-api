package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Role;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Status;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Team;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.User;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamService teamService;

    private User buildLead() {
        return User.builder()
                .id(1L).email("lead@test.com").role(Role.TEAM_LEAD).status(Status.ACTIF)
                .build();
    }

    private Team buildTeam(Long id, String name) {
        return Team.builder()
                .id(id).name(name).lead(buildLead()).members(new ArrayList<>())
                .build();
    }

    @Test
    void getTeam_existingId_returnsTeam() {
        Team team = buildTeam(1L, "Alpha");
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        Optional<Team> result = teamService.getTeam(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Alpha");
    }

    @Test
    void getTeam_unknownId_returnsEmpty() {
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        assertThat(teamService.getTeam(999L)).isEmpty();
    }

    @Test
    void getTeams_returnsAll() {
        when(teamRepository.findAll()).thenReturn(List.of(buildTeam(1L, "A"), buildTeam(2L, "B")));

        assertThat(teamService.getTeams()).hasSize(2);
    }

    @Test
    void getByLeadId_returnsList() {
        when(teamRepository.findByLeadId(1L)).thenReturn(List.of(buildTeam(1L, "A")));

        assertThat(teamService.getByLeadId(1L)).hasSize(1);
    }

    @Test
    void getByMembersId_returnsList() {
        when(teamRepository.findByMembersId(5L)).thenReturn(List.of(buildTeam(1L, "A")));

        assertThat(teamService.getByMembersId(5L)).hasSize(1);
    }

    @Test
    void saveTeam_callsRepository() {
        Team team = buildTeam(null, "New Team");
        Team saved = buildTeam(1L, "New Team");
        when(teamRepository.save(team)).thenReturn(saved);

        Team result = teamService.saveTeam(team);

        assertThat(result.getId()).isEqualTo(1L);
        verify(teamRepository).save(team);
    }

    @Test
    void deleteTeam_callsRepository() {
        teamService.deleteTeam(1L);

        verify(teamRepository, times(1)).deleteById(1L);
    }
}
