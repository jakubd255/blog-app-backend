package pl.jakubdudek.blogappbackend.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubdudek.blogappbackend.exception.ForbiddenException;
import pl.jakubdudek.blogappbackend.model.dto.response.IPostDto;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.util.mapper.DtoMapper;
import pl.jakubdudek.blogappbackend.model.dto.request.PostRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.PostDto;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enums.PostStatus;
import pl.jakubdudek.blogappbackend.model.enums.UserRole;
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

    public IPostDto getPost(Integer id) {
        IPostDto post = postRepository.findPostById(id, authenticationManager.getAuthenticatedUserId())
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if(post.getStatus().equals(PostStatus.DRAFT)) {
            requirePermissionToPost(post.getUser().getId());
        }
        return post;
    }

    public List<IPostDto> getPosts(PostStatus status, Integer userId) {
        return postRepository.findPostSummaries(status, userId, authenticationManager.getAuthenticatedUserId());
    }

    public PostDto editPost(Integer id, Post newPost) {
        Post post = findPostById(id);
        requirePermissionToPost(post.getUser().getId());

        post.setTitle(Optional.of(newPost.getTitle()).orElse(post.getTitle()));
        post.setBody(Optional.of(newPost.getBody()).orElse(post.getBody()));

        if(newPost.getStatus() == PostStatus.PUBLISHED && post.getStatus() == PostStatus.DRAFT) {
            post.setDate(new Date());
        }
        post.setStatus(Optional.of(newPost.getStatus()).orElse(post.getStatus()));

        return dtoMapper.mapPostToDto(postRepository.save(post));
    }

    public List<UserDto> getLikes(Integer id) {
        return postRepository.findUsersWhoLikedPost(id)
                .stream()
                .map(dtoMapper::mapUserToDto)
                .toList();
    }

    @Transactional
    public String likePost(Integer id) {
        User user = authenticationManager.getAuthenticatedUser();
        if(postRepository.isPostLikedByUser(id, user.getId()) == 0) {
            postRepository.likePost(id, user.getId());
            return "Successfully liked post: "+id;
        }
        else {
            postRepository.unlikePost(id, user.getId());
            return "Successfully unliked post: "+id;
        }
    }

    public void deletePost(Integer id) {
        Post post = findPostById(id);
        requirePermissionToPost(post.getUser().getId());

        postRepository.deleteById(id);
    }

    private Post findPostById(Integer id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    private void requirePermissionToPost(Integer authorId) {
        User user = authenticationManager.getAuthenticatedUser();

        if(user == null || !(user.getRole() == UserRole.ROLE_ADMIN || user.getId().equals(authorId))) {
            throw new ForbiddenException("You don't have permission to this post");
        }
    }
}
