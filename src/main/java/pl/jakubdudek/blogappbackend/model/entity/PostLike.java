package pl.jakubdudek.blogappbackend.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.jakubdudek.blogappbackend.model.key.PostLikeKey;

@Entity
@Table(name = "post_likes")
@Data
@NoArgsConstructor
public class PostLike {
    @EmbeddedId
    private PostLikeKey id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;

    public PostLike(User user, Post post) {
        this.id = new PostLikeKey(user.getId(), post.getId());
        this.user = user;
        this.post = post;
    }
}
