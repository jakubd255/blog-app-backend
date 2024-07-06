package pl.jakubdudek.blogappbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.jakubdudek.blogappbackend.model.dto.mapper.DtoMapper;
import pl.jakubdudek.blogappbackend.model.dto.request.LoginRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.JwtResponse;
import pl.jakubdudek.blogappbackend.model.dto.response.UserResponse;
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

    public JwtResponse logIn(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new BadCredentialsException("Invalid email")
        );

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return new JwtResponse(jwtGenerator.generateToken(user.getUsername()));
    }

    public UserResponse authenticate() {
        return dtoMapper.mapUserToDto(authenticationManager.getAuthenticatedUser());
    }
}
