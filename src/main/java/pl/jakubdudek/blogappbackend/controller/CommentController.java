package pl.jakubdudek.blogappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubdudek.blogappbackend.model.dto.request.CommentRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.CommentDto;
import pl.jakubdudek.blogappbackend.model.dto.response.ICommentDto;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/post/{id}")
    public ResponseEntity<CommentDto> addPostComment(@PathVariable Integer id, @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.addPostComment(id, request));
    }

    @PostMapping("/parent/{id}")
    public ResponseEntity<CommentDto> addCommentReply(@PathVariable Integer id, @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.addCommentReply(id, request));
    }

    @GetMapping("/post/{id}")
    public ResponseEntity<List<ICommentDto>> getPostComments(@PathVariable Integer id) {
        return ResponseEntity.ok(commentService.getPostComments(id));
    }

    @GetMapping("/parent/{id}")
    public ResponseEntity<List<ICommentDto>> getCommentReplies(@PathVariable Integer id) {
        return ResponseEntity.ok(commentService.getCommentReplies(id));
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<List<UserDto>> getLikes(@PathVariable Integer id) {
        return ResponseEntity.ok(commentService.getLikes(id));
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
