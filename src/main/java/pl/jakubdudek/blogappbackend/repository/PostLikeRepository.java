package pl.jakubdudek.blogappbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.jakubdudek.blogappbackend.model.entity.PostLike;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.key.PostLikeKey;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeKey> {

    @Query("SELECT l FROM PostLike l WHERE l.user.id = :userId AND l.post.id = :postId")
    Optional<PostLike> findByUserIdAndPostId(
            @Param("userId") Integer userId,
            @Param("postId") Integer postId
    );

    @Query("SELECT l.user FROM PostLike l WHERE l.post.id = :id")
    Page<User> findUsersWhoLikedPost(
            @Param("id") Integer id,
            Pageable pageable
    );
}
