package fr.univ_smb.info803.maturitymodelsassessmentsapi.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests du modèle User, notamment les méthodes héritées de UserDetails.
 * Pas besoin de Mockito ici : on teste directement l'objet Java.
 */
class UserTest {

    @Test
    void getUsername_returnsEmail() {
        User user = User.builder().email("test@test.com").build();

        // getUsername() doit retourner l'email (c'est l'identifiant unique)
        assertThat(user.getUsername()).isEqualTo("test@test.com");
    }

    @Test
    void getAuthorities_returnsRoleWithPrefix() {
        User user = User.builder().role(Role.PMO).build();

        // Le rôle doit être préfixé par "ROLE_" pour Spring Security
        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_PMO");
    }

    @Test
    void getAuthorities_teamLead() {
        User user = User.builder().role(Role.TEAM_LEAD).build();

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_TEAM_LEAD");
    }

    @Test
    void getAuthorities_teamMember() {
        User user = User.builder().role(Role.TEAM_MEMBER).build();

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_TEAM_MEMBER");
    }

    @Test
    void isEnabled_actifUser_returnsTrue() {
        User user = User.builder().status(Status.ACTIF).build();

        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void isEnabled_attenteUser_returnsFalse() {
        User user = User.builder().status(Status.ATTENTE).build();

        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void accountFlags_alwaysTrue() {
        User user = User.builder().build();

        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
    }
}
