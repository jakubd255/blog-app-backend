package pl.jakubdudek.blogappbackend.model.enums;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    ROLE_ADMIN,
    ROLE_REDACTOR,
    ROLE_USER;

    @Override
    public String getAuthority() {
        return name();
    }
}
