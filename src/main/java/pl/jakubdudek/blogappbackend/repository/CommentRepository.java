package pl.jakubdudek.blogappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.jakubdudek.blogappbackend.model.entity.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :id AND c.parent IS NULL")
    List<Comment> findCommentsByPostId(@Param("id") Integer id);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.parent.id = :id")
    List<Comment> findRepliesByParentId(@Param("id") Integer id);
}
