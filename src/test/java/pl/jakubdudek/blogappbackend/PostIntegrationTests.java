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
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enumerate.PostStatus;
import pl.jakubdudek.blogappbackend.model.enumerate.UserRole;
import pl.jakubdudek.blogappbackend.repository.PostRepository;
import pl.jakubdudek.blogappbackend.repository.UserRepository;
import pl.jakubdudek.blogappbackend.service.PostService;
import pl.jakubdudek.blogappbackend.util.jwt.JwtGenerator;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    public void addPostTest() {
        User admin = userRepository.findById(1).orElse(null);
        assert admin != null;

        String url = "http://localhost:"+port+"/api/posts";
        String authToken = "Bearer " + jwtGenerator.generateToken(admin.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PostRequest> request = new HttpEntity<>(new PostRequest("New post", "Post text", PostStatus.PUBLISHED), headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void getPostsTest() {
        User admin = userRepository.findById(1).orElse(null);
        assert admin != null;

        postRepository.save(Post.builder().title("post 1").body("test 1").status(PostStatus.PUBLISHED).user(admin).build());
        postRepository.save(Post.builder().title("post 2").body("test 2").status(PostStatus.DRAFT).user(admin).build());

        String url = "http://localhost:"+port+"/api/posts";

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<Map<String, Object>> posts = response.getBody();
        for(Map<String, Object> post : posts) {
            assertEquals("PUBLISHED", post.get("status"));
        }
    }

    @Test
    public void getAllPostsTest() {
        User admin = userRepository.findById(1).orElse(null);
        assert admin != null;

        postRepository.save(Post.builder().title("post 3").body("test 3").status(PostStatus.PUBLISHED).user(admin).build());
        postRepository.save(Post.builder().title("post 4").body("test 4").status(PostStatus.DRAFT).user(admin).build());

        String url = "http://localhost:"+port+"/api/posts/all";
        String authToken = "Bearer " + jwtGenerator.generateToken(admin.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getPostsForbiddenTest() {
        User user = userRepository.save(
                new User(null, "get.posts.forbidden.@gmail.com", "getPostsForbidden", "12345678", null, UserRole.ROLE_USER)
        );

        String url = "http://localhost:"+port+"/api/posts/all";
        String authToken = "Bearer " + jwtGenerator.generateToken(user.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void updatePostTest() {
        User user = userRepository.save(
                new User(null, "update.post@gmail.com", "updatePost", "12345678", null, UserRole.ROLE_ADMIN)
        );
        Post post = postRepository.save(
                Post.builder().title("Title to update").body("Text to update").status(PostStatus.DRAFT).user(user).build()
        );

        String url = "http://localhost:"+port+"/api/posts/"+post.getId();
        String authToken = "Bearer " + jwtGenerator.generateToken(user.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String title = "Updated Title";
        String body = "Updated text";
        PostStatus status = PostStatus.PUBLISHED;

        HttpEntity<PostRequest> request = new HttpEntity<>(new PostRequest(title, body, status), headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Post updatedPost = postRepository.findById(post.getId()).orElse(null);
        assert updatedPost != null;

        assertEquals(title, updatedPost.getTitle());
        assertEquals(body, updatedPost.getBody());
        assertEquals(status, updatedPost.getStatus());
    }

    @Test
    public void deletePostTest() {
        User user = userRepository.save(
                new User(null, "delete.post@gmail.com", "deletePost", "12345678", null, UserRole.ROLE_USER)
        );
        Post post = postRepository.save(
                Post.builder().title("Title").body("Text").status(PostStatus.PUBLISHED).user(user).build()
        );

        String url = "http://localhost:"+port+"/api/posts/"+post.getId();
        String authToken = "Bearer " + jwtGenerator.generateToken(user.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully deleted post: "+post.getId(), response.getBody());
    }

    @Test
    public void deletePostAsAdminTest() {
        User admin = userRepository.findById(1).orElse(null);
        assert admin != null;

        User user = userRepository.save(
                new User(null, "delete.post.admin.user@gmail.com", "deletePost", "12345678", null, UserRole.ROLE_USER)
        );

        Post post = postRepository.save(
                Post.builder().title("Title").body("Text").status(PostStatus.PUBLISHED).user(user).build()
        );

        String url = "http://localhost:"+port+"/api/posts/"+post.getId();
        String authToken = "Bearer " + jwtGenerator.generateToken(admin.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully deleted post: "+post.getId(), response.getBody());
    }

    @Test
    public void deletePostForbiddenTest() {
        User admin = userRepository.findById(1).orElse(null);
        assert admin != null;

        User user = userRepository.save(
                new User(null, "delete.post.forbidden@gmail.com", "deletePostForbidden", "12345678", null, UserRole.ROLE_USER)
        );

        Post post = postRepository.save(
                Post.builder().title("Title").body("Text").status(PostStatus.PUBLISHED).user(admin).build()
        );

        String url = "http://localhost:"+port+"/api/posts/"+post.getId();
        String authToken = "Bearer " + jwtGenerator.generateToken(user.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You don't have permission to delete this post", response.getBody());
    }
}
