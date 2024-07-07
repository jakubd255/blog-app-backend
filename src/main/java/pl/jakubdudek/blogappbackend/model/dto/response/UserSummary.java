package pl.jakubdudek.blogappbackend.model.dto.response;

import pl.jakubdudek.blogappbackend.model.role.UserRole;

public interface UserSummary {
    Integer getId();
    String getName();
    String getEmail();
    String getProfileImage();
    UserRole getRole();
}
