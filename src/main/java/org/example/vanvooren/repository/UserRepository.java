package org.example.vanvooren.repository;

import java.util.Optional;

import org.example.vanvooren.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
