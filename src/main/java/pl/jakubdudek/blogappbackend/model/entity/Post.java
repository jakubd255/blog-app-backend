package pl.jakubdudek.blogappbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import pl.jakubdudek.blogappbackend.model.enumerate.PostStatus;

import java.util.Date;

@Entity
@Table(name = "posts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Lob
    private String body;

    //@Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private Date date;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;
}
