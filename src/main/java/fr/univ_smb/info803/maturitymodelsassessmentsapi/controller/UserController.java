package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.user.UserRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.user.UserResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Role;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.User;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Gestion des utilisateurs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Créer un utilisateur", description = "Crée un utilisateur manuellement (hors flow d'inscription).")
    @ApiResponse(responseCode = "201", description = "Utilisateur créé")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        log.info("Création d'un nouvel utilisateur : {}", request.email());
        User user = userService.createFromRequest(request, passwordEncoder);
        User saved = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @Operation(summary = "Obtenir un utilisateur par id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable final long id) {
        log.info("Récupération de l'utilisateur id={}", id);
        return userService.getUser(id)
                .map(u -> ResponseEntity.ok(toResponse(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lister les utilisateurs par rôle", description = "Filtre les utilisateurs par rôle (PMO, TEAM_LEAD, TEAM_MEMBER).")
    @ApiResponse(responseCode = "200", description = "Liste filtrée")
    @GetMapping(params = "role")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@RequestParam Role role) {
        List<UserResponse> list = userService.getUsersByRole(role)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Obtenir plusieurs utilisateurs par ids", description = "Retourne les utilisateurs correspondant à la liste d'ids fournie dans le body.")
    @ApiResponse(responseCode = "200", description = "Liste des utilisateurs")
    @PostMapping("/batch")
    public ResponseEntity<List<UserResponse>> getUsersByIds(@RequestBody List<Long> ids) {
        List<UserResponse> list = userService.getUsersByIds(ids)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Lister tous les utilisateurs")
    @ApiResponse(responseCode = "200", description = "Liste de tous les utilisateurs")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getUsers().stream().map(this::toResponse).toList());
    }

    @Operation(summary = "Modifier un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable final Long id,
            @RequestBody UserRequest request) {

        return userService.getUser(id).map(current -> {
            if (request.firstName() != null) current.setFirstName(request.firstName());
            if (request.lastName()  != null) current.setLastName(request.lastName());
            if (request.email()     != null) current.setEmail(request.email());
            if (request.password()  != null) current.setPassword(passwordEncoder.encode(request.password()));
            if (request.role()      != null) current.setRole(request.role());
            if (request.status()    != null) current.setStatus(request.status());

            return ResponseEntity.ok(toResponse(userService.saveUser(current)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Supprimer un utilisateur")
    @ApiResponse(responseCode = "204", description = "Utilisateur supprimé")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable final Long id) {
        log.info("Suppression de l'utilisateur id={}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mon profil", description = "Retourne les informations de l'utilisateur connecté.")
    @ApiResponse(responseCode = "200", description = "Profil de l'utilisateur connecté")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        return ResponseEntity.ok(toResponse(user));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
