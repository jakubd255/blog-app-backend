package pl.jakubdudek.blogappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.jakubdudek.blogappbackend.model.dto.request.PostRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.PostDto;
import pl.jakubdudek.blogappbackend.model.dto.response.PostSummary;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDto> addPost(@RequestBody PostRequest post) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.addPost(post));
    }

    @GetMapping
    public ResponseEntity<List<PostSummary>> getAllPublishedPosts() {
        return ResponseEntity.ok(postService.getAllPublishedPosts());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PostSummary>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable Integer id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> editPost(@PathVariable Integer id, @RequestBody Post post) {
        return ResponseEntity.ok(postService.editPost(id, post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Integer id) {
        postService.deletePost(id);
        return ResponseEntity.ok("Successfully deleted post: "+id);
    }
}
