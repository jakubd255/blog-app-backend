package pl.jakubdudek.blogappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubdudek.blogappbackend.model.dto.response.PostResponse;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> addPost(@RequestBody Post post) {
        return ResponseEntity.ok(postService.addPost(post));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Integer id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> editPost(@PathVariable Integer id, @RequestBody Post post) {
        return ResponseEntity.ok(postService.editPost(id, post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Integer id) {
        postService.deletePost(id);
        return ResponseEntity.ok("Successfully deleted post: "+id);
    }
}
