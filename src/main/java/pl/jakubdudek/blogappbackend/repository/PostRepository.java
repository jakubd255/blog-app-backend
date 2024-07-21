package pl.jakubdudek.blogappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.jakubdudek.blogappbackend.model.dto.response.IPostDto;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enums.PostStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query("""
    SELECT
        p.id AS id, p.title AS title, p.date AS date, p.status AS status, p.user AS user, p.body as body,
        COUNT(DISTINCT l.id) AS likes,
        COUNT(DISTINCT c.id) as comments,
        (SUM(CASE WHEN l.id = :authUserId THEN 1 ELSE 0 END) > 0) AS isLiked
    FROM Post p
    LEFT JOIN p.likes l
    LEFT JOIN p.comments c ON c.parent IS NULL
    WHERE p.id = :id
    GROUP BY p.id
    ORDER BY p.date DESC
    """)
    Optional<IPostDto> findPostById(
            @Param("id") Integer id,
            @Param("authUserId") Integer authUserId
    );

    @Query("""
    SELECT
        p.id AS id, p.title AS title, p.date AS date, p.status AS status, p.user AS user,
        COUNT(DISTINCT l.id) AS likes,
        COUNT(DISTINCT c.id) as comments,
        (SUM(CASE WHEN l.id = :authUserId THEN 1 ELSE 0 END) > 0) AS isLiked
    FROM Post p
    LEFT JOIN p.likes l
    LEFT JOIN p.comments c ON c.parent IS NULL
    WHERE (:status IS NULL OR p.status = :status) AND (:userId IS NULL OR p.user.id = :userId)
    GROUP BY p.id
    ORDER BY p.date DESC
    """)
    List<IPostDto> findPostSummaries(
            @Param("status") PostStatus status,
            @Param("userId") Integer userId,
            @Param("authUserId") Integer authUserId
    );

    @Query(value = "SELECT COUNT(*) FROM post_likes WHERE post_id = :postId AND user_id = :userId", nativeQuery = true)
    int isPostLikedByUser(
            @Param("postId") Integer postId,
            @Param("userId") Integer userId
    );

    @Modifying
    @Query(value = "INSERT INTO post_likes (post_id, user_id) VALUES (:postId, :userId)", nativeQuery = true)
    void likePost(
            @Param("postId") Integer postId,
            @Param("userId") Integer userId
    );

    @Modifying
    @Query(value = "DELETE FROM post_likes WHERE post_id = :postId AND user_id = :userId", nativeQuery = true)
    void unlikePost(
            @Param("postId") Integer postId,
            @Param("userId") Integer userId
    );

    @Query("SELECT p.likes FROM Post p WHERE p.id = :id")
    List<User> findUsersWhoLikedPost(@Param("id") Integer id);
}