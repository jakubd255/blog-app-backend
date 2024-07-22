package pl.jakubdudek.blogappbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.jakubdudek.blogappbackend.model.dto.response.ICommentDto;
import pl.jakubdudek.blogappbackend.model.entity.Comment;
import pl.jakubdudek.blogappbackend.model.entity.User;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    @Query("""
    SELECT c.id AS id, c.text AS text, c.user AS user, c.date AS date, c.post.id AS postId, c.parent.id AS parentId,
        COUNT(DISTINCT l.id) AS likes,
        COUNT(DISTINCT r.id) as replies,
        (SUM(CASE WHEN l.id = :authUserId THEN 1 ELSE 0 END) > 0) AS isLiked
    FROM Comment c
    LEFT JOIN c.likes l
    LEFT JOIN c.replies r
    WHERE (:rootComments = FALSE OR c.parent IS NULL) AND (c.post.id = :postId OR c.parent.id = :parentId)
    GROUP BY c.id
    ORDER BY c.date DESC
    """)
    Page<ICommentDto> findComments(
            @Param("postId") Integer postId,
            @Param("parentId") Integer parentId,
            @Param("rootComments") Boolean rootComments,
            @Param("authUserId") Integer authUserId,
            Pageable pageable
    );

    @Query(value = "SELECT COUNT(*) FROM comment_likes WHERE comment_id = :commentId AND user_id = :userId", nativeQuery = true)
    int isCommentLikedByUser(
            @Param("commentId") Integer commentId,
            @Param("userId") Integer userId
    );

    @Modifying
    @Query(value = "INSERT INTO comment_likes (comment_id, user_id) VALUES (:commentId, :userId)", nativeQuery = true)
    void likeComment(
            @Param("commentId") Integer commentId,
            @Param("userId") Integer userId
    );

    @Modifying
    @Query(value = "DELETE FROM comment_likes WHERE comment_id = :commentId AND user_id = :userId", nativeQuery = true)
    void unlikeComment(
            @Param("commentId") Integer commentId,
            @Param("userId") Integer userId
    );

    @Query("SELECT c.likes FROM Comment c WHERE c.id = :id")
    Page<User> findUsersWhoLikedComment(
            @Param("id") Integer id,
            Pageable pageable
    );
}
