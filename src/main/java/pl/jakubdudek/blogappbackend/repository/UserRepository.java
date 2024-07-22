package pl.jakubdudek.blogappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enums.UserRole;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    int countByRole(UserRole role);
    Optional<User> findByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.role = COALESCE(:role, u.role) WHERE u.id = :id")
    int updateUserRole(
            @Param("id") Integer id,
            @Param("role") UserRole role
    );
}
