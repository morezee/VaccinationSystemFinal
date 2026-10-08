package com.immunecare.repository;

import com.immunecare.entity.User;
import com.immunecare.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    java.util.List<User> findByRole(Role role);
}
