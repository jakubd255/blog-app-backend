package pl.jakubdudek.blogappbackend.model.dto.mapper;

import org.springframework.stereotype.Component;
import pl.jakubdudek.blogappbackend.model.dto.response.PostDto;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.entity.User;

@Component
public class DtoMapper {

    public UserDto mapUserToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfileImage(),
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
}
