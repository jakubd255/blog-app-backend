package pl.jakubdudek.blogappbackend.model.dto.response;

import pl.jakubdudek.blogappbackend.model.enumerate.PostStatus;

import java.util.Date;

public interface PostSummary {
    Integer getId();
    String getTitle();
    Date getDate();
    UserSummary getUser();
    PostStatus getStatus();
}
