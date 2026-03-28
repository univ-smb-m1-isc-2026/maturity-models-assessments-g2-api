package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.UserRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.UserResponse;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Role;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.User;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create - Add a new user
     * @param user an object user
     * @return The user object saved
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        log.info("Création d'un nouvel utilisateur : {}", request.email());
        User user = userService.createFromRequest(request, passwordEncoder);
        User saved = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    /**
     * Read - Get one user
     * @param id is the id of the user
     * @return An user object fulfilled
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable final long id) {
        log.info("Récupération de l'utilisateur id={}", id);
        return userService.getUser(id)
                .map(u -> ResponseEntity.ok(toResponse(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Read - Get all users of one role
     * @param role is the role of the users
     * @return A list of users
     */
    @GetMapping(params = "role")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@RequestParam Role role) {
        List<UserResponse> list = userService.getUsersByRole(role)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    /**
     * Read - Get a list of users of different ids
     * @param ids is a list of users id
     * @return A list of users
     */
    @PostMapping("/batch")
    public ResponseEntity<List<UserResponse>> getUsersByIds(@RequestBody List<Long> ids) {
        List<UserResponse> list = userService.getUsersByIds(ids)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    /**
     * Read - Get all users
     * @return - A List object of User fulfilled
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getUsers().stream().map(this::toResponse).toList());
    }

    /**
     * Update - Update an existing user
     * @param id - The id of the user to update
     * @param request - The user object updated
     * @return The updated user
     */
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

    /**
     * Delete - Delete an user
     * @param id - The id of the user to delete
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable final Long id) {
        log.info("Suppression de l'utilisateur id={}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Read - Get the currently authenticated user
     * @param userDetails the authenticated user injected by Spring Security
     * @return The authenticated user object
     */
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
