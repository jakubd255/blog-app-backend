package pl.jakubdudek.blogappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enumerate.UserRole;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    int countByRole(UserRole role);
    Optional<User> findByEmail(String email);
}
