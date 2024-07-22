package pl.jakubdudek.blogappbackend;

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
import pl.jakubdudek.blogappbackend.model.dto.request.PostRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.PostDto;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enums.PostStatus;
import pl.jakubdudek.blogappbackend.model.enums.UserRole;
import pl.jakubdudek.blogappbackend.repository.PostRepository;
import pl.jakubdudek.blogappbackend.repository.UserRepository;
import pl.jakubdudek.blogappbackend.util.jwt.JwtGenerator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostIntegrationTests {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    public void testAddPost() {
        HttpEntity<PostRequest> request = new HttpEntity<>(
                new PostRequest("New post", "Post text", PostStatus.PUBLISHED),
                createAuthHeaders(getAdmin().getEmail())
        );

        ResponseEntity<PostDto> response = restTemplate.exchange(
                getUrl(""),
                HttpMethod.POST,
                request,
                PostDto.class
        );

        assertNotNull(response.getBody());

        Post newPost = postRepository.findById(response.getBody().id()).orElse(null);

        assertNotNull(newPost);
        assertNotNull(response.getBody());
        assertNotNull(newPost.getDate());
        assertNotNull(newPost.getId());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testAddPostForbidden() {
        User user = createUser(UserRole.ROLE_USER);

        HttpEntity<PostRequest> request = new HttpEntity<>(
                new PostRequest("New post", "Post text", PostStatus.PUBLISHED),
                createAuthHeaders(user.getEmail())
        );

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl(""),
                HttpMethod.POST,
                request,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testGetPosts() {
        User admin = getAdmin();

        createPost(admin, PostStatus.PUBLISHED);
        createPost(admin, PostStatus.DRAFT);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                getUrl(""),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<Map<String, Object>> posts = (List<Map<String, Object>>) response.getBody().get("content");
        for(Map<String, Object> post : posts) {
            assertEquals("PUBLISHED", post.get("status"));
        }
    }

    @Test
    public void testGetAllPosts() {
        User admin = getAdmin();

        createPost(admin, PostStatus.PUBLISHED);
        createPost(admin, PostStatus.DRAFT);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                getUrl("/all"),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(admin.getEmail())),
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testGetPostsForbidden() {
        User user = createUser(UserRole.ROLE_USER);

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/all"),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user.getEmail())),
                String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testGetPostsByUserId() {
        User user = createUser(UserRole.ROLE_REDACTOR);

        createPost(user, PostStatus.DRAFT);
        createPost(user, PostStatus.DRAFT);
        createPost(user, PostStatus.PUBLISHED);

        createPost(getAdmin(), PostStatus.DRAFT);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                getUrl("/user/"+user.getId()+"/all"),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user.getEmail())),
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Map<String, Object>> posts = (List<Map<String, Object>>) response.getBody().get("content");
        assertEquals(3, posts.size());
    }

    @Test
    public void testUpdatePost() {
        User user = createUser(UserRole.ROLE_REDACTOR);
        Post post = createPost(user, PostStatus.DRAFT);

        String title = "Updated Title";
        String body = "Updated text";
        PostStatus status = PostStatus.PUBLISHED;

        HttpEntity<PostRequest> request = new HttpEntity<>(
                new PostRequest(title, body, status),
                createAuthHeaders(user.getEmail())
        );

        ResponseEntity<PostDto> response = restTemplate.exchange(
                getUrl("/"+post.getId()),
                HttpMethod.PUT,
                request,
                PostDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Post updatedPost = postRepository.findById(post.getId()).orElse(null);

        assertNotNull(updatedPost);
        assertNotNull(updatedPost.getDate());
        assertEquals(title, updatedPost.getTitle());
        assertEquals(body, updatedPost.getBody());
        assertEquals(status, updatedPost.getStatus());

        if(post.getStatus().equals(PostStatus.DRAFT) && updatedPost.getStatus().equals(PostStatus.PUBLISHED)) {
            assertNotEquals(post.getDate(), updatedPost.getDate());
        }
        else {
            assertEquals(post.getDate(), updatedPost.getDate());
        }
    }

    @Test
    public void testDeletePost() {
        User user = createUser(UserRole.ROLE_REDACTOR);
        Post post = createPost(user, PostStatus.PUBLISHED);

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/"+post.getId()),
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthHeaders(user.getEmail())),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully deleted post: "+post.getId(), response.getBody());
    }

    @Test
    public void testDeletePostAsAdmin() {
        User user = createUser(UserRole.ROLE_USER);
        Post post = createPost(user, PostStatus.PUBLISHED);

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/"+post.getId()),
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthHeaders(getAdmin().getEmail())),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully deleted post: "+post.getId(), response.getBody());
    }

    @Test
    public void testDeletePostForbidden() {
        User user = createUser(UserRole.ROLE_REDACTOR);
        Post post = createPost(getAdmin(), PostStatus.PUBLISHED);

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/"+post.getId()),
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthHeaders(user.getEmail())),
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You don't have permission to this post", response.getBody());
    }

    private String getUrl(String route) {
        return "http://localhost:"+port+"/api/posts"+route;
    }

    private HttpHeaders createAuthHeaders(String email) {
        String authToken = "Bearer "+jwtGenerator.generateToken(email);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private User getAdmin() {
        User admin = userRepository.findById(1).orElse(null);
        assert admin != null;
        return admin;
    }

    private User createUser(UserRole role) {
        return userRepository.save(
                User.builder()
                        .email(UUID.randomUUID()+"@gmail.com")
                        .name("Test user")
                        .password("12345678")
                        .role(role)
                        .build()
        );
    }

    private Post createPost(User user, PostStatus status) {
        return postRepository.save(
                Post.builder()
                        .title(UUID.randomUUID().toString())
                        .body("Text")
                        .status(status)
                        .user(user)
                        .build()
        );
    }
}
