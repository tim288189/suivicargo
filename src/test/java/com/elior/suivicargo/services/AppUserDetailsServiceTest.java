package com.elior.suivicargo.services;

import com.elior.suivicargo.enums.Role;
import com.elior.suivicargo.models.User;
import com.elior.suivicargo.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private AppUserDetailsService service;

    @Test
    @DisplayName("loadUserByUsername() jette UsernameNotFoundException si utilisateur absent")
    void notFound() {
        when(userRepository.findByEmailIgnoreCase("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("x"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("loadUserByUsername() expose l'autorité ROLE_<role>")
    void rolePrefix() {
        User u = User.builder()
                .email("a@b.com").passwordHash("h").nom("N").prenom("P")
                .role(Role.SUPERVISOR).actif(true).build();
        when(userRepository.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("a@b.com");

        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_SUPERVISOR");
        assertThat(details.isEnabled()).isTrue();
    }
}
