package pl.jakubdudek.blogappbackend.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.role.UserRole;
import pl.jakubdudek.blogappbackend.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class AdminConfig {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password}")
    String adminPassword;

    @PostConstruct
    public void initialize() {
        if(userRepository.countByRole(UserRole.ROLE_ADMIN) == 0) {
            User admin = User.builder()
                    .name("Admin")
                    .email("admin@admin.com")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(UserRole.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("Admin has been created.");
        }
    }
}
