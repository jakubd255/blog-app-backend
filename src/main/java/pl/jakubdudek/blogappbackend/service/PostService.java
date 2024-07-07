package pl.jakubdudek.blogappbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubdudek.blogappbackend.exception.ForbiddenException;
import pl.jakubdudek.blogappbackend.model.dto.mapper.DtoMapper;
import pl.jakubdudek.blogappbackend.model.dto.response.PostDto;
import pl.jakubdudek.blogappbackend.model.dto.response.PostSummary;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.role.UserRole;
import pl.jakubdudek.blogappbackend.repository.PostRepository;
import pl.jakubdudek.blogappbackend.util.jwt.JwtAuthenticationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final JwtAuthenticationManager authenticationManager;
    private final DtoMapper dtoMapper;

    public PostDto addPost(pl.jakubdudek.blogappbackend.model.entity.Post post) {
        post.setUser(authenticationManager.getAuthenticatedUser());
        return dtoMapper.mapPostToDto(postRepository.save(post));
    }

    public PostDto getPost(Integer id) {
        pl.jakubdudek.blogappbackend.model.entity.Post post = postRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Post not found")
        );
        return dtoMapper.mapPostToDto(post);
    }

    public List<PostSummary> getAllPosts() {
        return postRepository.findAllWithoutBody();
    }

    public PostDto editPost(Integer id, pl.jakubdudek.blogappbackend.model.entity.Post newPost) {
        pl.jakubdudek.blogappbackend.model.entity.Post post = postRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Post not found")
        );

        if(isUserPermittedToPost(post)) {
            if(newPost.getTitle() != null && !newPost.getTitle().isEmpty()) {
                post.setTitle(newPost.getTitle());
            }
            if(newPost.getBody() != null && !newPost.getBody().isEmpty()) {
                post.setBody(newPost.getBody());
            }

            return dtoMapper.mapPostToDto(postRepository.save(post));
        }
        else {
            throw new ForbiddenException("You don't have permission to update this post");
        }
    }

    public void deletePost(Integer id) {
        pl.jakubdudek.blogappbackend.model.entity.Post post = postRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Post not found")
        );

        if(isUserPermittedToPost(post)) {
            postRepository.deleteById(id);
        }
        else {
            throw new ForbiddenException("You don't have permission to delete this post");
        }
    }

    private boolean isUserPermittedToPost(pl.jakubdudek.blogappbackend.model.entity.Post post) {
        User user = authenticationManager.getAuthenticatedUser();
        return user.getRole() == UserRole.ROLE_ADMIN || user.getId().equals(post.getUser().getId());
    }
}
