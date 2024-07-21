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
import pl.jakubdudek.blogappbackend.model.dto.request.CommentRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.CommentDto;
import pl.jakubdudek.blogappbackend.model.dto.response.ICommentDto;
import pl.jakubdudek.blogappbackend.model.dto.response.IPostDto;
import pl.jakubdudek.blogappbackend.model.entity.Comment;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enums.PostStatus;
import pl.jakubdudek.blogappbackend.model.enums.UserRole;
import pl.jakubdudek.blogappbackend.repository.CommentRepository;
import pl.jakubdudek.blogappbackend.repository.PostRepository;
import pl.jakubdudek.blogappbackend.repository.UserRepository;
import pl.jakubdudek.blogappbackend.service.CommentService;
import pl.jakubdudek.blogappbackend.service.PostService;
import pl.jakubdudek.blogappbackend.util.jwt.JwtGenerator;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentIntegrationTests {
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

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Test
    public void testAddComment() {
        Post post = createPost(getAdmin());

        HttpEntity<CommentRequest> request = new HttpEntity<>(
                new CommentRequest("New comment"),
                createAuthHeaders(getAdmin().getEmail())
        );

        ResponseEntity<CommentDto> response = restTemplate.exchange(
                getUrl("/post/"+post.getId()),
                HttpMethod.POST,
                request,
                CommentDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        List<IPostDto> posts = postService.getPosts(PostStatus.PUBLISHED, null);

        posts.forEach(p -> {
            if(p.getId().equals(post.getId())) {
                assertEquals(Long.valueOf(1), p.getComments());
            }
        });
    }

    @Test
    public void testAddReply() {
        Post post = createPost(getAdmin());
        Comment comment = createComment(getAdmin(), post);

        HttpEntity<CommentRequest> request = new HttpEntity<>(
                new CommentRequest("New comment"),
                createAuthHeaders(getAdmin().getEmail())
        );

        ResponseEntity<CommentDto> response = restTemplate.exchange(
                getUrl("/parent/"+comment.getId()),
                HttpMethod.POST,
                request,
                CommentDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        List<ICommentDto> comments = commentService.getCommentReplies(comment.getId());

        comments.forEach(c -> {
            if(c.getId().equals(comment.getId())) {
                assertEquals(Long.valueOf(1), c.getReplies());
            }
        });
    }

    private String getUrl(String route) {
        return "http://localhost:"+port+"/api/comments"+route;
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

    private Comment createComment(User user, Post post) {
        return commentRepository.save(
                Comment.builder()
                        .text(UUID.randomUUID().toString())
                        .user(user)
                        .post(post)
                        .build()
        );
    }

    private Comment createReply(User user, Comment comment) {
        return commentRepository.save(
                Comment.builder()
                        .text(UUID.randomUUID().toString())
                        .user(user)
                        .parent(comment)
                        .build()
        );
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

    private Post createPost(User user) {
        return postRepository.save(
                Post.builder()
                        .title(UUID.randomUUID().toString())
                        .body("Text")
                        .status(PostStatus.PUBLISHED)
                        .user(user)
                        .build()
        );
    }
}
