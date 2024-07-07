package pl.jakubdudek.blogappbackend.model.dto.response;

import pl.jakubdudek.blogappbackend.model.role.UserRole;

public record UserDto(
        Integer id,
        String name,
        String email,
        String profileImage,
        UserRole role
) {
}
