package pl.jakubdudek.blogappbackend.model.dto.response;

import pl.jakubdudek.blogappbackend.model.enumerate.UserRole;

public interface IUserSummaryDto {
    Integer getId();
    String getName();
    String getEmail();
    String getProfileImage();
    UserRole getRole();
}
