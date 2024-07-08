package pl.jakubdudek.blogappbackend.model.dto.response;

import pl.jakubdudek.blogappbackend.model.enumerate.PostStatus;

public interface PostDetailedSummary extends PostSummary {
    PostStatus getStatus();
}
