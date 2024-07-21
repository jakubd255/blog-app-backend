package pl.jakubdudek.blogappbackend;

import jakarta.persistence.EntityNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.jakubdudek.blogappbackend.model.dto.request.UserUpdateRequest;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enums.UserRole;
import pl.jakubdudek.blogappbackend.repository.UserRepository;
import pl.jakubdudek.blogappbackend.service.UserService;
import pl.jakubdudek.blogappbackend.util.jwt.JwtGenerator;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserIntegrationTests {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testGetAllUsers() {
        ResponseEntity<List> response = restTemplate.exchange(
                getUrl(""),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>(){}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotEquals(0, response.getBody().size());
    }

    @Test
    public void testGetUserNotFound() {
        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/0"),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testUserUpdate() {
        User user = createUser();

        String name = "Updated name";
        String bio = "Updated bio";

        HttpEntity<UserUpdateRequest> request = new HttpEntity<>(
                new UserUpdateRequest(name, bio),
                createAuthHeaders(user.getEmail())
        );

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/"+user.getId()),
                HttpMethod.PUT,
                request,
                String.class
        );

        User updatedUser = userRepository.findById(user.getId()).orElse(null);

        assertNotNull(updatedUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(name, updatedUser.getName());
        assertEquals(bio, updatedUser.getBio());
    }

    @Test
    public void testUpdateUserRole() {
        User user = createUser();

        HttpEntity<UserRole> request = new HttpEntity<>(
                UserRole.ROLE_REDACTOR,
                createAuthHeaders(getAdmin().getEmail())
        );

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/"+user.getId()+"/role"),
                HttpMethod.PUT,
                request,
                String.class
        );

        User updatedUser = userRepository.findById(user.getId()).orElse(null);

        assertNotNull(updatedUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(UserRole.ROLE_REDACTOR, updatedUser.getRole());
    }

    @Test
    public void testUpdateUserRoleNotFound() {
        createUser();

        HttpEntity<UserRole> request = new HttpEntity<>(
                UserRole.ROLE_REDACTOR,
                createAuthHeaders(getAdmin().getEmail())
        );

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/"+23472+"/role"),
                HttpMethod.PUT,
                request,
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }

    @Test
    public void testDeleteUser() {
        User user = createUser();

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/"+user.getId()),
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthHeaders(user.getEmail())),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThrows(EntityNotFoundException.class, () -> userService.getUser(user.getId()));
    }

    @Test
    public void testDeleteUserAsAdmin() {
        User admin = getAdmin();
        User user = createUser();

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/"+user.getId()),
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthHeaders(admin.getEmail())),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThrows(EntityNotFoundException.class, () -> userService.getUser(user.getId()));
    }

    @Test
    public void testDeleteUserForbidden() {
        User userToDelete = createUser();
        User authUser = createUser();

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/"+userToDelete.getId()),
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthHeaders(authUser.getEmail())),
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You don't have permission to this user", response.getBody());
    }

    private String getUrl(String route) {
        return "http://localhost:"+port+"/api/users"+route;
    }

    private HttpHeaders createAuthHeaders(String email) {
        String authToken = "Bearer "+jwtGenerator.generateToken(email);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private User createUser() {
        return userRepository.save(
                User.builder()
                        .email(UUID.randomUUID()+"@gmail.com")
                        .name("Test user")
                        .password("12345678")
                        .role(UserRole.ROLE_USER)
                        .build()
        );
    }

    private User getAdmin() {
        User admin = userRepository.findById(1).orElse(null);
        assert admin != null;
        return admin;
    }
}
