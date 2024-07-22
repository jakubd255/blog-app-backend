package pl.jakubdudek.blogappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubdudek.blogappbackend.model.dto.request.CommentRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.CommentDto;
import pl.jakubdudek.blogappbackend.model.dto.response.ICommentDto;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.service.CommentService;


@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/post/{id}")
    public ResponseEntity<CommentDto> addPostComment(@PathVariable Integer id, @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addPostComment(id, request));
    }

    @PostMapping("/parent/{id}")
    public ResponseEntity<CommentDto> addCommentReply(@PathVariable Integer id, @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addCommentReply(id, request));
    }

    @GetMapping("/post/{id}")
    public ResponseEntity<Page<ICommentDto>> getPostComments(@PathVariable Integer id, Pageable pageable) {
        return ResponseEntity.ok(commentService.getPostComments(id, pageable));
    }

    @GetMapping("/parent/{id}")
    public ResponseEntity<Page<ICommentDto>> getCommentReplies(@PathVariable Integer id, Pageable pageable) {
        return ResponseEntity.ok(commentService.getCommentReplies(id, pageable));
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<Page<UserDto>> getLikes(@PathVariable Integer id, Pageable pageable) {
        return ResponseEntity.ok(commentService.getLikes(id, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Integer id, @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.updateComment(id, request));
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<String> likeComment(@PathVariable Integer id) {
        return ResponseEntity.ok(commentService.likeComment(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Integer id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok("Successfully deleted comment: "+id);
    }
}
