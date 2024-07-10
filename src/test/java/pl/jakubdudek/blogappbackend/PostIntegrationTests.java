package pl.jakubdudek.blogappbackend;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.jakubdudek.blogappbackend.model.dto.request.PostRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.PostDto;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enumerate.PostStatus;
import pl.jakubdudek.blogappbackend.model.enumerate.UserRole;
import pl.jakubdudek.blogappbackend.repository.PostRepository;
import pl.jakubdudek.blogappbackend.repository.UserRepository;
import pl.jakubdudek.blogappbackend.util.jwt.JwtGenerator;

import java.util.List;
import java.util.Map;

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
    public void addPostTest() {
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
    public void addPostForbiddenTest() {
        User user = createUser("post.user@gmail.com", UserRole.ROLE_USER);

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
    public void getPostsTest() {
        User admin = getAdmin();

        postRepository.save(Post.builder().title("post 1").body("test 1").status(PostStatus.PUBLISHED).user(admin).build());
        postRepository.save(Post.builder().title("post 2").body("test 2").status(PostStatus.DRAFT).user(admin).build());

        createPost("Post 1", admin, PostStatus.PUBLISHED);
        createPost("Post 2", admin, PostStatus.DRAFT);

        ResponseEntity<List> response = restTemplate.exchange(
                getUrl(""),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                List.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<Map<String, Object>> posts = response.getBody();
        for(Map<String, Object> post : posts) {
            assertEquals("PUBLISHED", post.get("status"));
        }
    }

    @Test
    public void getAllPostsTest() {
        User admin = getAdmin();

        createPost("Post 3", admin, PostStatus.PUBLISHED);
        createPost("Post 4", admin, PostStatus.DRAFT);

        ResponseEntity<List> response = restTemplate.exchange(
                getUrl("/all"),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(admin.getEmail())),
                List.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getPostsForbiddenTest() {
        User user = createUser("get.posts.forbidden.@gmail.com", UserRole.ROLE_USER);

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/all"),
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(user.getEmail())),
                String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updatePostTest() {
        User user = createUser("update.post@gmail.com", UserRole.ROLE_USER);
        Post post = createPost("Title to update", user, PostStatus.DRAFT);

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
    public void deletePostTest() {
        User user = createUser("delete.post@gmail.com", UserRole.ROLE_USER);
        Post post = createPost("Title to update", user, PostStatus.PUBLISHED);

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
    public void deletePostAsAdminTest() {
        User user = createUser("delete.post.admin.user@gmail.com", UserRole.ROLE_USER);
        Post post = createPost("Title", user, PostStatus.PUBLISHED);

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
    public void deletePostForbiddenTest() {
        User user = createUser("delete.post.forbidden@gmail.com", UserRole.ROLE_USER);
        Post post = createPost("Title", getAdmin(), PostStatus.PUBLISHED);

        ResponseEntity<String> response = restTemplate.exchange(
                getUrl("/"+post.getId()),
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthHeaders(user.getEmail())),
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You don't have permission to delete this post", response.getBody());
    }

    private String getUrl(String route) {
        return "http://localhost:"+port+"/api/posts"+route;
    }

    private HttpHeaders createAuthHeaders(String email) {
        String authToken = "Bearer "+jwtGenerator.generateToken(email);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        return headers;
    }

    private User getAdmin() {
        User admin = userRepository.findById(1).orElse(null);
        assert admin != null;
        return admin;
    }

    private User createUser(String email, UserRole role) {
        return userRepository.save(
                User.builder()
                        .email(email)
                        .name("Test user")
                        .password("12345678")
                        .role(role)
                        .build()
        );
    }

    private Post createPost(String title, User user, PostStatus status) {
        return postRepository.save(
                Post.builder()
                        .title(title)
                        .body("Text")
                        .status(status)
                        .user(user)
                        .build()
        );
    }
}
