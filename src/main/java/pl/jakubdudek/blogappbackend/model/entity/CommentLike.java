package pl.jakubdudek.blogappbackend.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.jakubdudek.blogappbackend.model.key.CommentLikeKey;

@Entity
@Table(name = "comment_likes")
@Data
@NoArgsConstructor
public class CommentLike {
    @EmbeddedId
    private CommentLikeKey id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("commentId")
    @JoinColumn(name = "comment_id")
    private Comment comment;

    public CommentLike(User user, Comment comment) {
        this.id = new CommentLikeKey(user.getId(), comment.getId());
        this.user = user;
        this.comment = comment;
    }
}
