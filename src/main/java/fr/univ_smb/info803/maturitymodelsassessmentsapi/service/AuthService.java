package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.AuthResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.LoginRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.RegisterRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.*;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.TeamRepository;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final InvitationService invitationService;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user freely, hash the password and return a JWT token.
     * The role is chosen by the user (PMO, TEAM_LEAD, TEAM_MEMBER).
     */
    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(Status.ACTIF)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    /**
     * Register a new user via an invitation token.
     * The role is forced to TEAM_MEMBER regardless of the request body.
     * The user is automatically added to the team linked to the invitation.
     * The invitation is marked as ACCEPTED once the user is saved.
     * @param request the registration form filled by the invited user
     * @param invitationToken the UUID token from the invitation link
     */
    public AuthResponse registerWithInvitation(RegisterRequest request, String invitationToken) {
        Invitation invitation = invitationService.validateToken(invitationToken);

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(invitation.getEmail())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.TEAM_MEMBER)
                .status(Status.ACTIF)
                .build();

        User saved = userRepository.save(user);

        Team team = invitation.getTeam();
        team.getMembers().add(saved);
        teamRepository.save(team);

        invitationService.consumeInvitation(invitationToken);

        String jwtToken = jwtService.generateToken(saved);
        return new AuthResponse(jwtToken);
    }

    /**
     * Authenticate a user and return a JWT token
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
