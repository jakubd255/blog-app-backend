package pl.jakubdudek.blogappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.jakubdudek.blogappbackend.model.entity.Comment;
import pl.jakubdudek.blogappbackend.model.entity.User;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :id AND c.parent IS NULL")
    List<Comment> findCommentsByPostId(@Param("id") Integer id);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.parent.id = :id")
    List<Comment> findRepliesByParentId(@Param("id") Integer id);

    @Query(value = "SELECT COUNT(*) FROM comment_likes WHERE comment_id = :commentId AND user_id = :userId", nativeQuery = true)
    int isCommentLikedByUser(@Param("commentId") Integer commentId, @Param("userId") Integer userId);

    @Modifying
    @Query(value = "INSERT INTO comment_likes (comment_id, user_id) VALUES (:commentId, :userId)", nativeQuery = true)
    void likeComment(@Param("commentId") Integer commentId, @Param("userId") Integer userId);

    @Modifying
    @Query(value = "DELETE FROM comment_likes WHERE comment_id = :commentId AND user_id = :userId", nativeQuery = true)
    void unlikeComment(@Param("commentId") Integer commentId, @Param("userId") Integer userId);



    @Query("SELECT c.likes FROM Comment c WHERE c.id = :id")
    List<User> findUsersWhoLikedComment(@Param("id") Integer id);
}
