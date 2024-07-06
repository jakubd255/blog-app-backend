package pl.jakubdudek.blogappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.jakubdudek.blogappbackend.model.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
}
