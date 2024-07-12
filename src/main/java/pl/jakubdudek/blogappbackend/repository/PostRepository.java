package pl.jakubdudek.blogappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.jakubdudek.blogappbackend.model.dto.response.IPostSummaryDto;
import pl.jakubdudek.blogappbackend.model.entity.Post;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query("SELECT p.id AS id, p.title AS title, p.date AS date, p.status AS status, p.user AS user FROM Post p WHERE status = 'PUBLISHED'")
    List<IPostSummaryDto> findPublishedPostSummaries();

    @Query("SELECT p.id AS id, p.title AS title, p.date AS date, p.status AS status, p.user AS user FROM Post p")
    List<IPostSummaryDto> findPostSummaries();

    @Query("SELECT p.id AS id, p.title AS title, p.date AS date, p.status AS status, p.user AS user FROM Post p WHERE user.id = :id AND status = 'PUBLISHED'")
    List<IPostSummaryDto> findPublishedPostSummariesByUserId(@Param("id") Integer id);

    @Query("SELECT p.id AS id, p.title AS title, p.date AS date, p.status AS status, p.user AS user FROM Post p WHERE user.id = :id")
    List<IPostSummaryDto> findPostSummariesByUserId(@Param("id") Integer id);
}