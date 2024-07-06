package pl.jakubdudek.blogappbackend.model.dto.response;

import pl.jakubdudek.blogappbackend.model.role.UserRole;

public record UserResponse(
        Integer id,
        String name,
        String email,
        UserRole role
) {
}
