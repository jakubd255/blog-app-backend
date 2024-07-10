package pl.jakubdudek.blogappbackend.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubdudek.blogappbackend.model.dto.request.LoginRequest;
import pl.jakubdudek.blogappbackend.model.dto.request.PasswordUpdateRequest;
import pl.jakubdudek.blogappbackend.model.dto.request.EmailUpdateRequest;
import pl.jakubdudek.blogappbackend.model.dto.request.RegisterRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.Jwt;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.service.AuthenticationService;
import pl.jakubdudek.blogappbackend.util.cookie.CookieManager;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final CookieManager cookieManager;

    @PostMapping("/register")
    public ResponseEntity<Jwt> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        Jwt token = authenticationService.register(request);
        cookieManager.addCookie(response, token.token());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/log-in")
    public ResponseEntity<Jwt> logIn(@RequestBody LoginRequest request, HttpServletResponse response) {
        Jwt token = authenticationService.logIn(request);
        cookieManager.addCookie(response, token.token());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/log-out")
    public ResponseEntity<String> logOut(HttpServletResponse response) {
        cookieManager.removeCookies(response);
        return ResponseEntity.ok("Successfully logged out");
    }

    @GetMapping
    public ResponseEntity<UserDto> authenticate() {
        return ResponseEntity.ok(authenticationService.authenticate());
    }

    @PutMapping("/email")
    public ResponseEntity<Jwt> updateEmail(@RequestBody EmailUpdateRequest request, HttpServletResponse response) {
        Jwt token = authenticationService.updateEmail(request);
        cookieManager.addCookie(response, token.token());
        return ResponseEntity.ok(token);
    }

    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateRequest request) {
        authenticationService.updatePassword(request);
        return ResponseEntity.ok("Successfully updated password");
    }
}
