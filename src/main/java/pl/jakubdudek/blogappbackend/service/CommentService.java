package pl.jakubdudek.blogappbackend.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubdudek.blogappbackend.exception.ForbiddenException;
import pl.jakubdudek.blogappbackend.model.dto.request.CommentRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.CommentDto;
import pl.jakubdudek.blogappbackend.model.dto.response.ICommentDto;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.model.entity.Comment;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enums.UserRole;
import pl.jakubdudek.blogappbackend.repository.CommentRepository;
import pl.jakubdudek.blogappbackend.util.jwt.JwtAuthenticationManager;
import pl.jakubdudek.blogappbackend.util.mapper.DtoMapper;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
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

    public List<ICommentDto> getPostComments(Integer id) {
        return commentRepository.findComments(id, null, true, authenticationManager.getAuthenticatedUserId());
    }

    public List<ICommentDto> getCommentReplies(Integer id) {
        return commentRepository.findComments(null, id, false, authenticationManager.getAuthenticatedUserId());
    }

    public List<UserDto> getLikes(Integer id) {
        return commentRepository.findUsersWhoLikedComment(id)
                .stream()
                .map(dtoMapper::mapUserToDto)
                .toList();
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
        if(commentRepository.isCommentLikedByUser(id, user.getId()) == 0) {
            commentRepository.likeComment(id, user.getId());
            return "Successfully liked comment: "+id;
        }
        else {
            commentRepository.unlikeComment(id, user.getId());
            return "Successfully unliked comment: "+id;
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
