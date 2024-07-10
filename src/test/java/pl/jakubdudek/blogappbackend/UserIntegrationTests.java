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
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enumerate.UserRole;
import pl.jakubdudek.blogappbackend.repository.UserRepository;
import pl.jakubdudek.blogappbackend.service.UserService;
import pl.jakubdudek.blogappbackend.util.jwt.JwtGenerator;

import java.util.List;

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
    public void getAllUsersTest() {
        ResponseEntity<List<UserDto>> response = restTemplate.exchange(
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
    public void getUserNotFoundTest() {
        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/13"),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void deleteUserTest() {
        User user = createUser("delete.user@gmail.com");

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
    public void deleteUserAsAdminTest() {
        User admin = getAdmin();
        User user = createUser("delete.user.admin@gmail.com");

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
    public void deleteUserForbiddenTest() {
        User userToDelete = createUser("delete.user.forbidden1.com");
        User authUser = createUser("delete.user.forbidden2.com");

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/"+userToDelete.getId()),
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthHeaders(authUser.getEmail())),
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You don't have permission to delete this user", response.getBody());
    }

    private String getUrl(String route) {
        return "http://localhost:"+port+"/api/users"+route;
    }

    private HttpHeaders createAuthHeaders(String email) {
        String authToken = "Bearer "+jwtGenerator.generateToken(email);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        return headers;
    }

    private User createUser(String email) {
        return userRepository.save(
                User.builder()
                        .email(email)
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
