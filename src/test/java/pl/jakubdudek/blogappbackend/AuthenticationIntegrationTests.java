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
        User user = userRepository.save(
                new User(null, "authentication@gmail.com", "authentication", "12345678", null, UserRole.ROLE_USER)
        );

        String url = "http://localhost:"+port+"/api/auth";
        String authToken = "Bearer " + jwtGenerator.generateToken(user.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);

        ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), UserDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertEquals(user.getId(), response.getBody().id());
    }

    @Test
    public void authenticationInvalidTokenTest() {
        String url = "http://localhost:"+port+"/api/auth";
        String authToken = "Bearer jkhjkhjd.as2kasdhkjh345sadsadasdasfsdfsdfsd32hv4hj2gv34asdajgfasd234hj.gasd23hj4";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void authenticationUserNotFoundTest() {
        String url = "http://localhost:"+port+"/api/auth";
        String authToken = "Bearer " + jwtGenerator.generateToken("not.found@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void updatePasswordTest() {
        String oldPassword = "12345678";
        String newPassword = "new_password";

        User user = userRepository.save(
                new User(null, "update.password@gmail.com", "updatePassword", passwordEncoder.encode(oldPassword), null, UserRole.ROLE_USER)
        );

        String url = "http://localhost:"+port+"/api/auth/password";
        String authToken = "Bearer " + jwtGenerator.generateToken(user.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PasswordUpdateRequest> request = new HttpEntity<>(new PasswordUpdateRequest(oldPassword, newPassword), headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
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

        User user = userRepository.save(
                new User(null, oldEmail, "updateEmail", "12345678", null, UserRole.ROLE_USER)
        );

        String url = "http://localhost:"+port+"/api/auth/email";
        String authToken = "Bearer " + jwtGenerator.generateToken(user.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EmailUpdateRequest> request = new HttpEntity<>(new EmailUpdateRequest(newEmail), headers);

        ResponseEntity<Jwt> response = restTemplate.exchange(url, HttpMethod.PUT, request, Jwt.class);
        User updatedUser = userRepository.findByEmail(newEmail).orElse(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(updatedUser);
        assertEquals(newEmail, updatedUser.getEmail());
    }

    @Test
    public void duplicateEmailTest() {
        String email = "taken.email@gmail.com";

        User user1 = userRepository.save(
                new User(null, email, "duplicateEmail", "12345678", null, UserRole.ROLE_USER)
        );

        assertThrows(DataIntegrityViolationException.class, () -> {
            User user2 = userRepository.save(
                    new User(null, email, "duplicateEmail", "12345678", null, UserRole.ROLE_USER)
            );
        });
    }
}
