package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.dto.user.UserRequest;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Role;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.Status;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.User;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserService.
 *
 * @ExtendWith(MockitoExtension.class) = on utilise Mockito pour simuler les dépendances.
 * @Mock = crée un faux objet (ici le repository) qui ne touche pas la vraie base de données.
 * @InjectMocks = injecte les mocks dans le service qu'on teste.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // --- Helpers pour créer des objets de test ---

    private User buildUser(Long id, String email, Role role) {
        return User.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .password("encodedPassword")
                .role(role)
                .status(Status.ACTIF)
                .build();
    }

    // --- Tests ---

    @Test
    void getUser_existingId_returnsUser() {
        // GIVEN : on configure le mock pour retourner un user quand on cherche l'id 1
        User user = buildUser(1L, "john@test.com", Role.PMO);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // WHEN : on appelle la méthode à tester
        Optional<User> result = userService.getUser(1L);

        // THEN : on vérifie le résultat
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void getUser_unknownId_returnsEmpty() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUser(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void getUserByEmail_existingEmail_returnsUser() {
        User user = buildUser(1L, "john@test.com", Role.PMO);
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail("john@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void getUsersByRole_returnsList() {
        List<User> teamLeads = List.of(
                buildUser(1L, "lead1@test.com", Role.TEAM_LEAD),
                buildUser(2L, "lead2@test.com", Role.TEAM_LEAD)
        );
        when(userRepository.findByRole(Role.TEAM_LEAD)).thenReturn(teamLeads);

        List<User> result = userService.getUsersByRole(Role.TEAM_LEAD);

        assertThat(result).hasSize(2);
    }

    @Test
    void getUsers_returnsAll() {
        when(userRepository.findAll()).thenReturn(List.of(
                buildUser(1L, "a@test.com", Role.PMO),
                buildUser(2L, "b@test.com", Role.TEAM_MEMBER)
        ));

        List<User> result = userService.getUsers();

        assertThat(result).hasSize(2);
    }

    @Test
    void getUsersByIds_returnsList() {
        List<Long> ids = List.of(1L, 2L);
        when(userRepository.findAllById(ids)).thenReturn(List.of(
                buildUser(1L, "a@test.com", Role.PMO),
                buildUser(2L, "b@test.com", Role.TEAM_LEAD)
        ));

        List<User> result = userService.getUsersByIds(ids);

        assertThat(result).hasSize(2);
    }

    @Test
    void saveUser_callsRepository() {
        User user = buildUser(null, "new@test.com", Role.PMO);
        User saved = buildUser(1L, "new@test.com", Role.PMO);
        when(userRepository.save(user)).thenReturn(saved);

        User result = userService.saveUser(user);

        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).save(user); // vérifie que save() a bien été appelé
    }

    @Test
    void deleteUser_callsRepository() {
        userService.deleteUser(1L);

        // On vérifie que deleteById a été appelé exactement 1 fois avec l'id 1
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void createFromRequest_buildsUserCorrectly() {
        UserRequest request = new UserRequest("Jane", "Doe", "jane@test.com", "password123", Role.TEAM_MEMBER, Status.ACTIF);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode("password123")).thenReturn("encodedPassword123");

        User result = userService.createFromRequest(request, encoder);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("jane@test.com");
        assertThat(result.getPassword()).isEqualTo("encodedPassword123");
        assertThat(result.getRole()).isEqualTo(Role.TEAM_MEMBER);
        assertThat(result.getStatus()).isEqualTo(Status.ACTIF);
    }
}
