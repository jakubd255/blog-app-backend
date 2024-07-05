package pl.jakubdudek.blogappbackend.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubdudek.blogappbackend.model.dto.request.LoginRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.JwtResponse;
import pl.jakubdudek.blogappbackend.model.dto.response.UserResponse;
import pl.jakubdudek.blogappbackend.service.AuthenticationService;
import pl.jakubdudek.blogappbackend.util.cookie.CookieManager;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final CookieManager cookieManager;

    @PostMapping("/log-in")
    public ResponseEntity<JwtResponse> logIn(@RequestBody LoginRequest request, HttpServletResponse response) {
        JwtResponse token = authenticationService.logIn(request);
        cookieManager.addCookie(response, token.token());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/log-out")
    public ResponseEntity<String> logOut(HttpServletResponse response) {
        cookieManager.removeCookies(response);
        return ResponseEntity.ok("Successfully logged out");
    }

    @GetMapping
    public ResponseEntity<UserResponse> authenticate() {
        return ResponseEntity.ok(authenticationService.authenticate());
    }
}
