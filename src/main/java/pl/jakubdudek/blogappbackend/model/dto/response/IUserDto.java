package pl.jakubdudek.blogappbackend.model.dto.response;

import pl.jakubdudek.blogappbackend.model.enumerate.UserRole;

public interface IUserDto {
    Integer getId();
    String getName();
    String getEmail();
    String getProfileImage();
    UserRole getRole();
}
