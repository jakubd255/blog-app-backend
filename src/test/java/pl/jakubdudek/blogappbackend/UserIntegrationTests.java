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
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
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
        String url = "http://localhost:"+port+"/api/users";
        ResponseEntity<List<UserDto>> response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<>(){});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotEquals(0, Objects.requireNonNull(response.getBody()).size());
    }

    @Test
    public void getUserNotFoundTest() {
        String url = "http://localhost:"+port+"/api/users/13";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void deleteUserTest() {
        User user = userRepository.save(
                new User(null, "delete.user@gmail.com", "deleteUser", "12345678", null, UserRole.ROLE_USER)
        );

        String url = "http://localhost:"+port+"/api/users/"+user.getId();
        String authToken = "Bearer " + jwtGenerator.generateToken(user.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThrows(EntityNotFoundException.class, () -> userService.getUser(user.getId()));
    }

    @Test
    public void deleteUserAsAdminTest() {
        User admin = userRepository.findById(1).orElse(null);
        assert admin != null;

        User user = userRepository.save(
                new User(null, "delete.user.admin@gmail.com", "deleteUserAsAdmin", "12345678", null, UserRole.ROLE_USER)
        );

        String url = "http://localhost:"+port+"/api/users/"+user.getId();
        String authToken = "Bearer " + jwtGenerator.generateToken(admin.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThrows(EntityNotFoundException.class, () -> userService.getUser(user.getId()));
    }

    @Test
    public void deleteUserForbiddenTest() {
        User userToDelete = userRepository.save(
                new User(null, "delete.user.forbidden1.com", "deleteUserForbidden", "12345678", null, UserRole.ROLE_USER)
        );
        User authUser = userRepository.save(
                new User(null, "delete.user.forbidden2.com", "deleteUserForbidden", "12345678", null, UserRole.ROLE_USER)
        );

        String url = "http://localhost:"+port+"/api/users/"+userToDelete.getId();
        String authToken = "Bearer " + jwtGenerator.generateToken(authUser.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You don't have permission to delete this user", response.getBody());
    }
}
