package pl.jakubdudek.blogappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.jakubdudek.blogappbackend.model.dto.request.PostRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.IPostDto;
import pl.jakubdudek.blogappbackend.model.dto.response.IUserDto;
import pl.jakubdudek.blogappbackend.model.dto.response.PostDto;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.model.enums.PostStatus;
import pl.jakubdudek.blogappbackend.service.PostService;


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
    public ResponseEntity<Page<IPostDto>> getAllPublishedPosts(Pageable pageable) {
        return ResponseEntity.ok(postService.getPosts(PostStatus.PUBLISHED, null, pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<IPostDto>> getAllPosts(Pageable pageable) {
        return ResponseEntity.ok(postService.getPosts(null, null, pageable));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('REDACTOR')")
    @GetMapping("/user/{id}/all")
    public ResponseEntity<Page<IPostDto>> getAllPostsByUserId(@PathVariable Integer id, Pageable pageable) {
        return ResponseEntity.ok(postService.getPosts(null, id, pageable));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Page<IPostDto>> getAllPublishedPostsByUserId(@PathVariable Integer id, Pageable pageable) {
        return ResponseEntity.ok(postService.getPosts(PostStatus.PUBLISHED, id, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IPostDto> getPost(@PathVariable Integer id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<Page<UserDto>> getLikes(@PathVariable Integer id, Pageable pageable) {
        return ResponseEntity.ok(postService.getLikes(id, pageable));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('REDACTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<PostDto> editPost(@PathVariable Integer id, @RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.editPost(id, request));
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
