package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.UserDto;
import com.elior.suivicargo.enums.Role;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.UserMapper;
import com.elior.suivicargo.models.User;
import com.elior.suivicargo.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository repository;
    @Mock private UserMapper mapper;
    @InjectMocks private UserService service;

    @Test
    @DisplayName("softDelete() refuse de supprimer un ADMIN (FORBIDDEN)")
    void softDelete_admin_refuse() {
        User admin = User.builder().role(Role.ADMIN).build();
        when(repository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.softDelete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ADMIN");
    }

    @Test
    @DisplayName("softDelete() désactive le compte et flag supprime=true")
    void softDelete_employee_ok() {
        User u = User.builder().role(Role.EMPLOYEE).actif(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(u));

        service.softDelete(1L);

        assertThat(u.isSupprime()).isTrue();
        assertThat(u.isActif()).isFalse();
        verify(repository).save(u);
    }

    @Test
    @DisplayName("getById() jette NOT_FOUND si supprimé")
    void getById_supprime() {
        User u = new User();
        u.setSupprime(true);
        when(repository.findById(1L)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> service.getById(1L)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("update() applique le mapper et sauve")
    void update_ok() {
        User u = User.builder().role(Role.EMPLOYEE).build();
        when(repository.findById(1L)).thenReturn(Optional.of(u));
        when(repository.save(u)).thenReturn(u);
        when(mapper.toDto(u)).thenReturn(mock(UserDto.class));

        service.update(1L, null);

        verify(mapper).updateEntity(any(), eq(u));
        verify(repository).save(u);
    }
}
