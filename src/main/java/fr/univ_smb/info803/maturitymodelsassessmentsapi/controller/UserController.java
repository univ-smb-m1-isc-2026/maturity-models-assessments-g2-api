package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.enums.Role;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.User;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * Create - Add a new user
     * @param user an object user
     * @return The user object saved
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(user));
    }

    /**
     * Read - Get one user
     * @param id is the id of the user
     * @return An user object fulfilled
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable final long id){
        Optional<User> user = userService.getUser(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Read - Get all users of one role
     * @param role is the role of the users
     * @return A list of users
     */
    @GetMapping(params = "role")
    public ResponseEntity<List<User>> getUsersByRole(@RequestParam Role role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    /**
     * Read - Get a list of users of different ids
     * @param ids is a list of users id
     * @return A list of users
     */
    @PostMapping("/batch")
    public ResponseEntity<List<User>> getUsersByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(userService.getUsersByIds(ids));
    }

    /**
     * Read - Get all users
     * @return - A List object of User fulfilled
     */
    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    /**
     * Update - Update an existing user
     * @param id - The id of the user to update
     * @param user - The user object updated
     * @return The updated user
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable final Long id, @RequestBody User user) {
        Optional<User> u = userService.getUser(id);
        if(u.isPresent()) {
            User currentUser = u.get();

            if(user.getFirstName() != null)  currentUser.setFirstName(user.getFirstName());
            if(user.getLastName() != null)   currentUser.setLastName(user.getLastName());
            if(user.getEmail() != null)      currentUser.setEmail(user.getEmail());
            if(user.getPassword() != null)   currentUser.setPassword(user.getPassword());
            if(user.getRole() != null)       currentUser.setRole(user.getRole());
            if(user.getStatus() != null)     currentUser.setStatus(user.getStatus());

            userService.saveUser(currentUser);
            return ResponseEntity.ok(currentUser);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Delete - Delete an user
     * @param id - The id of the user to delete
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable final Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
