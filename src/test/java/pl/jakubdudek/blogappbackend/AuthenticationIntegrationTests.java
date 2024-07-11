package pl.jakubdudek.blogappbackend;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.jakubdudek.blogappbackend.model.dto.request.EmailUpdateRequest;
import pl.jakubdudek.blogappbackend.model.dto.request.PasswordUpdateRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.Jwt;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enumerate.UserRole;
import pl.jakubdudek.blogappbackend.repository.UserRepository;
import pl.jakubdudek.blogappbackend.util.jwt.JwtGenerator;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationIntegrationTests {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void authenticationTest() {
        User user = createUser("authentication@gmail.com", "12345678");

        ResponseEntity<UserDto> response = restTemplate.exchange(
                getUrl(""),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user.getEmail())),
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(user.getId(), response.getBody().id());
    }

    @Test
    public void authenticationInvalidTokenTest() {
        String authToken = "Bearer jkhjkhjd.as2kasdhkjh345sadsadasdasfsdfsdfsd32hv4hj2gv34asdajgfasd234hj.gasd23hj4";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl(""),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void authenticationUserNotFoundTest() {
        ResponseEntity<String> response = restTemplate.exchange(
                getUrl(""),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders("not.found@gmail.com")),
                String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void updatePasswordTest() {
        String oldPassword = "12345678";
        String newPassword = "new_password";

        User user = createUser("update.password@gmail.com", oldPassword);

        HttpEntity<PasswordUpdateRequest> request = new HttpEntity<>(
                new PasswordUpdateRequest(oldPassword, newPassword),
                createAuthHeaders(user.getEmail())
        );

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/password"),
                HttpMethod.PUT,
                request,
                String.class
        );

        User updatedUser = userRepository.findByEmail(user.getEmail()).orElse(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully updated password", response.getBody());
        assertNotNull(updatedUser);
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @Test
    public void updateEmailTest() {
        String oldEmail = "old.email@gmail.com";
        String newEmail = "new.email@gmail.com";

        User user = createUser(oldEmail, "12345678");

        HttpEntity<EmailUpdateRequest> request = new HttpEntity<>(
                new EmailUpdateRequest(newEmail),
                createAuthHeaders(user.getEmail())
        );

        ResponseEntity<Jwt> response = restTemplate.exchange(
                getUrl("/email"),
                HttpMethod.PUT,
                request,
                Jwt.class
        );

        User updatedUser = userRepository.findByEmail(newEmail).orElse(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(updatedUser);
        assertEquals(newEmail, updatedUser.getEmail());
    }

    @Test
    public void duplicateEmailTest() {
        String email = "taken.email@gmail.com";
        createUser(email, "12345678");
        assertThrows(DataIntegrityViolationException.class, () -> {
            createUser(email, "12345678");
        });
    }

    private String getUrl(String route) {
        return "http://localhost:"+port+"/api/auth"+route;
    }

    private HttpHeaders createAuthHeaders(String email) {
        String authToken = "Bearer "+jwtGenerator.generateToken(email);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private User createUser(String email, String password) {
        return userRepository.save(
                User.builder()
                        .email(email)
                        .name("Test user")
                        .password(passwordEncoder.encode(password))
                        .role(UserRole.ROLE_USER)
                        .build()
        );
    }
}
