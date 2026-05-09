package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.UpdateUserRequest;
import com.elior.suivicargo.dtos.UserDto;
import com.elior.suivicargo.enums.Role;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.UserMapper;
import com.elior.suivicargo.models.User;
import com.elior.suivicargo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Transactional(readOnly = true)
    public Page<UserDto> list(Role role, Pageable pageable) {
        if (role != null) {
            return repository.findByRoleAndSupprimeFalse(role, pageable).map(mapper::toDto);
        }
        return repository.findBySupprimeFalse(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        return mapper.toDto(findOrThrow(id));
    }

    @Transactional
    public UserDto update(Long id, UpdateUserRequest req) {
        User u = findOrThrow(id);
        mapper.updateEntity(req, u);
        return mapper.toDto(repository.save(u));
    }

    @Transactional
    public void softDelete(Long id) {
        User u = findOrThrow(id);
        if (u.getRole() == Role.ADMIN) {
            // Sécurité : on n'autorise pas la suppression d'un autre admin via cette API.
            throw BusinessException.forbidden("CANNOT_DELETE_ADMIN",
                    "Impossible de supprimer un compte ADMIN");
        }
        u.setSupprime(true);
        u.setActif(false);
        repository.save(u);
    }

    private User findOrThrow(Long id) {
        User u = repository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND",
                        "Utilisateur introuvable : " + id));
        if (u.isSupprime()) {
            throw BusinessException.notFound("USER_NOT_FOUND", "Utilisateur supprimé : " + id);
        }
        return u;
    }
}
