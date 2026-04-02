package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.auth.AuthResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.auth.LoginRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.auth.RegisterRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.*;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.TeamRepository;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private InvitationService invitationService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_createsUserAndReturnsToken() {
        // GIVEN
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@test.com", "pass123", Role.PMO);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token-123");

        // WHEN
        AuthResponse response = authService.register(request);

        // THEN
        assertThat(response.token()).isEqualTo("jwt-token-123");

        // On capture l'utilisateur sauvegardé pour vérifier ses champs
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("john@test.com");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPass"); // mot de passe encodé
        assertThat(savedUser.getRole()).isEqualTo(Role.PMO);
        assertThat(savedUser.getStatus()).isEqualTo(Status.ACTIF);
    }

    @Test
    void registerWithInvitation_forcesTeamMemberRole() {
        // GIVEN : une invitation valide
        User lead = User.builder().id(10L).email("lead@test.com").build();
        Team team = Team.builder().id(1L).name("Alpha").lead(lead).members(new ArrayList<>()).build();
        Invitation invitation = Invitation.builder()
                .id(1L).email("invited@test.com").token("token-abc")
                .team(team).invitedBy(lead).status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        RegisterRequest request = new RegisterRequest("Jane", "Doe", "ignored@test.com", "pass", Role.PMO);

        when(invitationService.validateToken("token-abc")).thenReturn(invitation);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-invite-token");

        // WHEN
        AuthResponse response = authService.registerWithInvitation(request, "token-abc");

        // THEN
        assertThat(response.token()).isEqualTo("jwt-invite-token");

        // Le rôle doit être forcé à TEAM_MEMBER (même si la request dit PMO)
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.TEAM_MEMBER);

        // L'email doit venir de l'invitation, pas de la request
        assertThat(captor.getValue().getEmail()).isEqualTo("invited@test.com");

        // L'invitation doit être marquée comme consommée
        verify(invitationService).consumeInvitation("token-abc");

        // Le user doit être ajouté aux membres de l'équipe
        assertThat(team.getMembers()).hasSize(1);
    }

    @Test
    void login_authenticatesAndReturnsToken() {
        // GIVEN
        LoginRequest request = new LoginRequest("john@test.com", "pass123");
        User user = User.builder().id(1L).email("john@test.com").password("encodedPass")
                .role(Role.PMO).status(Status.ACTIF).build();

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-login-token");

        // WHEN
        AuthResponse response = authService.login(request);

        // THEN
        assertThat(response.token()).isEqualTo("jwt-login-token");
        // Vérifie que l'AuthenticationManager a bien été appelé (validation mot de passe)
        verify(authenticationManager).authenticate(any());
    }
}
