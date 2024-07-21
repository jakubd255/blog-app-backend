package pl.jakubdudek.blogappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.jakubdudek.blogappbackend.model.dto.request.PostRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.IPostDto;
import pl.jakubdudek.blogappbackend.model.dto.response.IPostSummaryDto;
import pl.jakubdudek.blogappbackend.model.dto.response.PostDto;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('REDACTOR')")
    public ResponseEntity<PostDto> addPost(@RequestBody PostRequest post) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.addPost(post));
    }

    @GetMapping
    public ResponseEntity<List<IPostSummaryDto>> getAllPublishedPosts() {
        return ResponseEntity.ok(postService.getAllPublishedPosts());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IPostSummaryDto>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('REDACTOR')")
    @GetMapping("/user/{id}/all")
    public ResponseEntity<List<IPostSummaryDto>> getAllPostsByUserId(@PathVariable Integer id) {
        return ResponseEntity.ok(postService.getAllPostsByUserId(id));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<IPostSummaryDto>> getAllPublishedPostsByUserId(@PathVariable Integer id) {
        return ResponseEntity.ok(postService.getAllPublishedPostsByUserId(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IPostDto> getPost(@PathVariable Integer id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<List<UserDto>> getLikes(@PathVariable Integer id) {
        return ResponseEntity.ok(postService.getLikes(id));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('REDACTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<PostDto> editPost(@PathVariable Integer id, @RequestBody Post post) {
        return ResponseEntity.ok(postService.editPost(id, post));
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<String> likePost(@PathVariable Integer id) {
        return ResponseEntity.ok(postService.likePost(id));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('REDACTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Integer id) {
        postService.deletePost(id);
        return ResponseEntity.ok("Successfully deleted post: "+id);
    }
}
