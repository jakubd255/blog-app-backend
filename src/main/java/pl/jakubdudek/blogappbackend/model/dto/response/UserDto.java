package pl.jakubdudek.blogappbackend.model.dto.response;

import pl.jakubdudek.blogappbackend.model.enums.UserRole;

public record UserDto(
        Integer id,
        String name,
        String email,
        String profileImage,
        String bio,
        UserRole role
) {
}
