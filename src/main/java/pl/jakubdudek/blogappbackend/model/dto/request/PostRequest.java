package pl.jakubdudek.blogappbackend.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.jakubdudek.blogappbackend.model.enums.PostStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {
    private String title;
    private String body;
    private PostStatus status;
}
