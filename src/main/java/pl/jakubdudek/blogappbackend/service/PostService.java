package pl.jakubdudek.blogappbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubdudek.blogappbackend.exception.ForbiddenException;
import pl.jakubdudek.blogappbackend.model.dto.mapper.DtoMapper;
import pl.jakubdudek.blogappbackend.model.dto.request.PostRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.PostDto;
import pl.jakubdudek.blogappbackend.model.dto.response.PostSummary;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enumerate.PostStatus;
import pl.jakubdudek.blogappbackend.model.enumerate.UserRole;
import pl.jakubdudek.blogappbackend.repository.PostRepository;
import pl.jakubdudek.blogappbackend.util.jwt.JwtAuthenticationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final JwtAuthenticationManager authenticationManager;
    private final DtoMapper dtoMapper;

    public PostDto addPost(PostRequest request) {
        System.out.println(authenticationManager.getAuthenticatedUser());
        Post post = Post.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .status(request.getStatus())
                .user(authenticationManager.getAuthenticatedUser())
                .build();
        return dtoMapper.mapPostToDto(postRepository.save(post));
    }

    public PostDto getPost(Integer id) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Post not found")
        );

        if(post.getStatus().equals(PostStatus.DRAFT) && !isUserPermittedToPost(post)) {
            throw new ForbiddenException("Only admin or author has access to drafts");
        }

        return dtoMapper.mapPostToDto(post);
    }

    public List<PostSummary> getAllPublishedPosts() {
        return postRepository.findAllPublishedPostSummaries();
    }

    public List<PostSummary> getAllPosts() {
        return postRepository.findAllPostSummaries();
    }

    public PostDto editPost(Integer id, Post newPost) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Post not found")
        );

        if(isUserPermittedToPost(post)) {
            if(newPost.getTitle() != null && !newPost.getTitle().isEmpty()) {
                post.setTitle(newPost.getTitle());
            }
            if(newPost.getBody() != null && !newPost.getBody().isEmpty()) {
                post.setBody(newPost.getBody());
            }
            if(newPost.getStatus() != null) {
                post.setStatus(newPost.getStatus());
            }

            return dtoMapper.mapPostToDto(postRepository.save(post));
        }
        else {
            throw new ForbiddenException("You don't have permission to update this post");
        }
    }

    public void deletePost(Integer id) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Post not found")
        );

        if(isUserPermittedToPost(post)) {
            postRepository.deleteById(id);
        }
        else {
            throw new ForbiddenException("You don't have permission to delete this post");
        }
    }

    private boolean isUserPermittedToPost(Post post) {
        if(!authenticationManager.isUserAuthenticated()) {
            return false;
        }

        User user = authenticationManager.getAuthenticatedUser();
        return user.getRole() == UserRole.ROLE_ADMIN || user.getId().equals(post.getUser().getId());
    }
}
