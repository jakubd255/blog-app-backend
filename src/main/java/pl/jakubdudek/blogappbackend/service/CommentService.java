package pl.jakubdudek.blogappbackend.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.jakubdudek.blogappbackend.exception.ForbiddenException;
import pl.jakubdudek.blogappbackend.model.dto.request.CommentRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.CommentDto;
import pl.jakubdudek.blogappbackend.model.dto.response.ICommentDto;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.model.entity.Comment;
import pl.jakubdudek.blogappbackend.model.entity.CommentLike;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enums.UserRole;
import pl.jakubdudek.blogappbackend.repository.CommentLikeRepository;
import pl.jakubdudek.blogappbackend.repository.CommentRepository;
import pl.jakubdudek.blogappbackend.util.jwt.JwtAuthenticationManager;
import pl.jakubdudek.blogappbackend.util.mapper.DtoMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final DtoMapper dtoMapper;
    private final JwtAuthenticationManager authenticationManager;

    public CommentDto addPostComment(Integer postId, CommentRequest request) {
        Comment comment = Comment.builder()
                .user(authenticationManager.getAuthenticatedUser())
                .text(request.getText())
                .post(Post.builder().id(postId).build())
                .build();

        return dtoMapper.mapCommentToDto(commentRepository.save(comment));
    }

    public CommentDto addCommentReply(Integer parentId, CommentRequest request) {
        Comment parent = findCommentById(parentId);

        Comment reply = Comment.builder()
                .user(authenticationManager.getAuthenticatedUser())
                .text(request.getText())
                .post(parent.getPost())
                .parent(parent)
                .build();

        return dtoMapper.mapCommentToDto(commentRepository.save(reply));
    }

    public Page<ICommentDto> getPostComments(Integer id, Pageable pageable) {
        Integer authUserId = authenticationManager.getAuthenticatedUserId();
        return commentRepository.findComments(id, null, true, authUserId, pageable);
    }

    public Page<ICommentDto> getCommentReplies(Integer id, Pageable pageable) {
        Integer authUserId = authenticationManager.getAuthenticatedUserId();
        return commentRepository.findComments(null, id, false, authUserId, pageable);
    }

    public Page<UserDto> getLikes(Integer id, Pageable pageable) {
        return dtoMapper.mapUsersToDto(commentLikeRepository.findUsersWhoLikedComment(id, pageable));
    }

    public CommentDto updateComment(Integer id, CommentRequest request) {
        Comment comment = findCommentById(id);
        requirePermissionToComment(comment);

        comment.setText(Optional.of(request.getText()).orElse(comment.getText()));

        return dtoMapper.mapCommentToDto(commentRepository.save(comment));
    }

    @Transactional
    public String likeComment(Integer id) {
        User user = authenticationManager.getAuthenticatedUser();
        Optional<CommentLike> existingLike = commentLikeRepository.findByUserIdAndCommentId(user.getId(), id);

        if(existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            return "Successfully unliked comment: "+id;
        }
        else {
            Comment comment = Comment.builder().id(id).build();
            commentLikeRepository.save(new CommentLike(user, comment));
            return "Successfully liked comment: "+id;
        }
    }

    public void deleteComment(Integer id) {
        Comment comment = findCommentById(id);
        requirePermissionToComment(comment);
        commentRepository.deleteById(id);
    }

    private void requirePermissionToComment(Comment comment) {
        User user = authenticationManager.getAuthenticatedUser();

        if(user.getRole() == UserRole.ROLE_ADMIN) {
            return;
        }
        if(comment.getUser().getId().equals(user.getId())) {
            return;
        }
        if(comment.getPost().getUser().getId().equals(user.getId())) {
            return;
        }

        throw new ForbiddenException("You don't have permission to this comment");
    }

    private Comment findCommentById(Integer id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
    }
}
