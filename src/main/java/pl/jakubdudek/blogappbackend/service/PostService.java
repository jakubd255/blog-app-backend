package pl.jakubdudek.blogappbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubdudek.blogappbackend.exception.ForbiddenException;
import pl.jakubdudek.blogappbackend.util.mapper.DtoMapper;
import pl.jakubdudek.blogappbackend.model.dto.request.PostRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.PostDto;
import pl.jakubdudek.blogappbackend.model.dto.response.IPostSummaryDto;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enumerate.PostStatus;
import pl.jakubdudek.blogappbackend.model.enumerate.UserRole;
import pl.jakubdudek.blogappbackend.repository.PostRepository;
import pl.jakubdudek.blogappbackend.util.jwt.JwtAuthenticationManager;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final JwtAuthenticationManager authenticationManager;
    private final DtoMapper dtoMapper;

    public PostDto addPost(PostRequest request) {
        return dtoMapper.mapPostToDto(
                postRepository.save(Post.builder()
                        .title(request.getTitle())
                        .body(request.getBody())
                        .status(request.getStatus())
                        .user(authenticationManager.getAuthenticatedUser())
                        .build()
        ));
    }

    public PostDto getPost(Integer id) {
        Post post = findPostById(id);
        if(post.getStatus().equals(PostStatus.DRAFT)) {
            requirePermissionToPost(post);
        }
        return dtoMapper.mapPostToDto(post);
    }

    public List<IPostSummaryDto> getAllPublishedPosts() {
        return postRepository.findPublishedPostSummaries();
    }

    public List<IPostSummaryDto> getAllPosts() {
        return postRepository.findPostSummaries();
    }

    public List<IPostSummaryDto> getAllPublishedPostsByUserId(Integer id) {
        return postRepository.findPublishedPostSummariesByUserId(id);
    }

    public List<IPostSummaryDto> getAllPostsByUserId(Integer id) {
        return postRepository.findPostSummariesByUserId(id);
    }

    public PostDto editPost(Integer id, Post newPost) {
        Post post = findPostById(id);
        requirePermissionToPost(post);

        post.setTitle(Optional.of(newPost.getTitle()).orElse(post.getTitle()));
        post.setBody(Optional.of(newPost.getBody()).orElse(post.getBody()));

        if(newPost.getStatus() == PostStatus.PUBLISHED && post.getStatus() == PostStatus.DRAFT) {
            post.setDate(new Date());
        }
        post.setStatus(Optional.of(newPost.getStatus()).orElse(post.getStatus()));

        return dtoMapper.mapPostToDto(postRepository.save(post));
    }

    public void deletePost(Integer id) {
        Post post = findPostById(id);
        requirePermissionToPost(post);

        postRepository.deleteById(id);
    }

    private Post findPostById(Integer id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    private void requirePermissionToPost(Post post) {
        User user = authenticationManager.getAuthenticatedUser();

        if(user == null || !(user.getRole() == UserRole.ROLE_ADMIN || user.getId().equals(post.getUser().getId()))) {
            throw new ForbiddenException("You don't have permission to this post");
        }
    }
}
