package pl.jakubdudek.blogappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.jakubdudek.blogappbackend.model.dto.response.PostSummary;
import pl.jakubdudek.blogappbackend.model.entity.Post;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query("SELECT p.id AS id, p.title AS title, p.date AS date, p.user AS user FROM Post p")
    List<PostSummary> findAllWithoutBody();
}
