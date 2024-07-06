package pl.jakubdudek.blogappbackend.model.dto.mapper;

import org.springframework.stereotype.Component;
import pl.jakubdudek.blogappbackend.model.dto.response.PostResponse;
import pl.jakubdudek.blogappbackend.model.dto.response.UserResponse;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.entity.User;

@Component
public class DtoMapper {
    public UserResponse mapUserToDto(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public PostResponse mapPostToDto(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getBody(),
                post.getDate(),
                mapUserToDto(post.getUser())
        );
    }
}
