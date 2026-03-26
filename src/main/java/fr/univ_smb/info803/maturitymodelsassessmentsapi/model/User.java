package fr.univ_smb.info803.maturitymodelsassessmentsapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.enums.Role;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToMany(mappedBy = "members")
    @JsonIgnore
    @Builder.Default
    private List<Team> teams = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    /* -- Fonctions issues de UserDetails -- */
    /**
     * Return the user's role (ROLE_PMO, ROLE_TEAM_LEAD, ...)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Return unique id - an email
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Return account status
     */
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}


