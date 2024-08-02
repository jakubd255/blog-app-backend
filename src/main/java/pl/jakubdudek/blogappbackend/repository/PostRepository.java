package pl.jakubdudek.blogappbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.jakubdudek.blogappbackend.model.dto.response.IPostDto;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.enums.PostStatus;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query("""
    SELECT
        p.id AS id, p.title AS title, p.date AS date, p.status AS status, p.user AS user, p.body as body,
        COUNT(l.post.id) AS likes,
        COUNT(DISTINCT c.id) as comments,
        (SUM(CASE WHEN l.user.id = :authUserId THEN 1 ELSE 0 END) > 0) AS isLiked
    FROM Post p
    LEFT JOIN p.likes l
    LEFT JOIN p.comments c ON c.parent IS NULL
    WHERE p.id = :id
    GROUP BY p.id
    """)
    Optional<IPostDto> findPostById(
            @Param("id") Integer id,
            @Param("authUserId") Integer authUserId
    );

    @Query("""
    SELECT
        p.id AS id, p.title AS title, p.date AS date, p.status AS status, p.user AS user,
        COUNT(l.post.id) AS likes,
        COUNT(DISTINCT c.id) as comments,
        (SUM(CASE WHEN l.user.id = :authUserId THEN 1 ELSE 0 END) > 0) AS isLiked
    FROM Post p
    LEFT JOIN p.likes l
    LEFT JOIN p.comments c ON c.parent IS NULL
    WHERE (:status IS NULL OR p.status = :status) AND (:userId IS NULL OR p.user.id = :userId)
    GROUP BY p.id
    ORDER BY p.date DESC
    """)
    Page<IPostDto> findPostSummaries(
            @Param("status") PostStatus status,
            @Param("userId") Integer userId,
            @Param("authUserId") Integer authUserId,
            Pageable pageable
    );
}