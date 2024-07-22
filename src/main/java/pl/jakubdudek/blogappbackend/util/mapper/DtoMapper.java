package pl.jakubdudek.blogappbackend.util.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import pl.jakubdudek.blogappbackend.model.dto.response.CommentDto;
import pl.jakubdudek.blogappbackend.model.dto.response.PostDto;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.model.entity.Comment;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.entity.User;

import java.util.List;

@Component
public class DtoMapper {

    public UserDto mapUserToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfileImage(),
                user.getBio(),
                user.getRole()
        );
    }

    public PostDto mapPostToDto(Post post) {
        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getBody(),
                post.getStatus(),
                post.getDate(),
                mapUserToDto(post.getUser())
        );
    }

    public CommentDto mapCommentToDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                mapUserToDto(comment.getUser()),
                comment.getDate()
        );
    }

    public Page<UserDto> mapUsersToDto(Page<User> page) {
        List<UserDto> users = page.stream().map(this::mapUserToDto).toList();
        return new PageImpl<>(users, page.getPageable(), page.getTotalElements());
    }
}
