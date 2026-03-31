package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.auth.AuthResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.team.InvitationResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.auth.LoginRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.auth.RegisterRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Invitation;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.AuthService;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.InvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Inscription et authentification")
public class AuthController {

    private final AuthService authService;
    private final InvitationService invitationService;

    @Operation(summary = "Inscription libre", description = "Crée un compte PMO ou TEAM_LEAD. Le rôle est choisi librement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compte créé, JWT retourné"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Inscription via invitation", description = "Crée un compte TEAM_MEMBER à partir d'un token d'invitation. Le rôle est forcé à TEAM_MEMBER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compte créé, JWT retourné"),
            @ApiResponse(responseCode = "400", description = "Token invalide ou expiré")
    })
    @PostMapping("/register/{token}")
    public ResponseEntity<AuthResponse> registerWithInvitation(
            @RequestBody RegisterRequest request,
            @PathVariable String token) {
        return ResponseEntity.ok(authService.registerWithInvitation(request, token));
    }

    @Operation(summary = "Valider un token d'invitation", description = "Vérifie qu'un token est valide et retourne les infos de l'invitation (email, équipe). À appeler avant d'afficher le formulaire d'inscription.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token valide"),
            @ApiResponse(responseCode = "400", description = "Token invalide, expiré ou déjà utilisé")
    })
    @GetMapping("/invite/{token}")
    public ResponseEntity<InvitationResponse> validateInvitation(@PathVariable String token) {
        Invitation inv = invitationService.validateToken(token);
        return ResponseEntity.ok(new InvitationResponse(
                inv.getId(),
                inv.getEmail(),
                inv.getToken(),
                inv.getTeam().getName(),
                inv.getInvitedBy().getEmail(),
                inv.getStatus(),
                inv.getExpiresAt(),
                inv.getCreatedAt()
        ));
    }

    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentification réussie, JWT retourné"),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}