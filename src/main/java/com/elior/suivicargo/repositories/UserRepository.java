package com.elior.suivicargo.repositories;

import com.elior.suivicargo.enums.Role;
import com.elior.suivicargo.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<User> findByRoleAndSupprimeFalse(Role role, Pageable pageable);

    Page<User> findBySupprimeFalse(Pageable pageable);
}
