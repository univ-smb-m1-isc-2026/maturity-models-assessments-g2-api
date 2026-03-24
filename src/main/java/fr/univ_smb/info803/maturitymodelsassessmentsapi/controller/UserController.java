package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.User;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public User createUser(@RequestBody User user){ return userService.saveUser(user); }

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
     * Read - Get one user
     * @param email is the email of the user
     * @return An user object fulfilled
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUser(@PathVariable final String email){
        Optional<User> user = userService.getByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Read - Get all users
     * @return - An Iterable object of User fulfilled
     */
    @GetMapping
    public Iterable<User> getUsers() {
        return userService.getUsers();
    }

    /**
     * Update - Update an existing user
     * @param id - The id of the user to update
     * @param user - The user object updated
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateEmployee(@PathVariable final Long id, @RequestBody User user) {
        Optional<User> u = userService.getUser(id);
        if(u.isPresent()) {
            User currentUser = u.get();

            String firstName = user.getFirstName();
            if(firstName != null) {
                currentUser.setFirstName(firstName);
            }
            String lastName = user.getLastName();
            if(lastName != null) {
                currentUser.setLastName(lastName);
            }
            String mail = user.getEmail();
            if(mail != null) {
                currentUser.setEmail(mail);
            }
            String password = user.getPassword();
            if(password != null) {
                currentUser.setPassword(password);
            }
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
