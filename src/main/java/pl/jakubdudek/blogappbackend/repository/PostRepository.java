package pl.jakubdudek.blogappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.jakubdudek.blogappbackend.model.dto.response.IPostSummaryDto;
import pl.jakubdudek.blogappbackend.model.entity.Post;
import pl.jakubdudek.blogappbackend.model.enums.PostStatus;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query("""
    SELECT p.id AS id, p.title AS title, p.date AS date, p.status AS status, p.user AS user
    FROM Post p
    WHERE (:status IS NULL OR p.status = :status) AND (:userId IS NULL OR p.user.id = :userId)
    ORDER BY p.date DESC
    """)
    List<IPostSummaryDto> findPostSummaries(@Param("status") PostStatus status, @Param("userId") Integer userId);
}