package pl.jakubdudek.blogappbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.jakubdudek.blogappbackend.util.mapper.DtoMapper;
import pl.jakubdudek.blogappbackend.model.dto.request.LoginRequest;
import pl.jakubdudek.blogappbackend.model.dto.request.PasswordUpdateRequest;
import pl.jakubdudek.blogappbackend.model.dto.request.EmailUpdateRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.Jwt;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.repository.UserRepository;
import pl.jakubdudek.blogappbackend.util.jwt.JwtAuthenticationManager;
import pl.jakubdudek.blogappbackend.util.jwt.JwtGenerator;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtGenerator jwtGenerator;
    private final JwtAuthenticationManager authenticationManager;
    private final DtoMapper dtoMapper;

    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );
    }

    public Jwt logIn(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new BadCredentialsException("Invalid email")
        );

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return new Jwt(jwtGenerator.generateToken(user.getUsername()));
    }

    public UserDto authenticate() {
        return dtoMapper.mapUserToDto(authenticationManager.getAuthenticatedUser());
    }

    public Jwt updateEmail(EmailUpdateRequest request) {
        User user = authenticationManager.getAuthenticatedUser();

        user.setEmail(request.getEmail());
        userRepository.save(user);

        return new Jwt(jwtGenerator.generateToken(user.getUsername()));
    }

    public void updatePassword(PasswordUpdateRequest request) {
        User user = authenticationManager.getAuthenticatedUser();

        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
