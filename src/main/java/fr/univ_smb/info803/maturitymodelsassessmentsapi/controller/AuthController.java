package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.AuthResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.LoginRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.RegisterRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Invitation;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.AuthService;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final InvitationService invitationService;

    /**
     * Register a new user and return a JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Register a new user via an invitation token.
     * The role is forced to TEAM_MEMBER, no profile selection available.
     */
    @PostMapping("/register/{token}")
    public ResponseEntity<AuthResponse> registerWithInvitation(
            @RequestBody RegisterRequest request,
            @PathVariable String token) {
        return ResponseEntity.ok(authService.registerWithInvitation(request, token));
    }

    /**
     * Validate an invitation token before displaying the registration form.
     * Returns the invitation details (email, team name) if the token is valid.
     */
    @GetMapping("/invite/{token}")
    public ResponseEntity<Invitation> validateInvitation(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.validateToken(token));
    }

    /**
     * Authenticate a user and return a JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}