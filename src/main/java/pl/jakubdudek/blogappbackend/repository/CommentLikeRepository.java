package pl.jakubdudek.blogappbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.jakubdudek.blogappbackend.model.entity.CommentLike;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.key.CommentLikeKey;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeKey> {

    @Query("SELECT l FROM CommentLike l WHERE l.user.id = :userId AND l.comment.id = :commentId")
    Optional<CommentLike> findByUserIdAndCommentId(
            @Param("userId") Integer userId,
            @Param("commentId") Integer commentId
    );

    @Query("SELECT l.user FROM CommentLike l WHERE l.comment.id = :id")
    Page<User> findUsersWhoLikedComment(
            @Param("id") Integer id,
            Pageable pageable
    );
}
