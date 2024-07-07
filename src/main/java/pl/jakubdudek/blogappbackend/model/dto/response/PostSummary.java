package pl.jakubdudek.blogappbackend.model.dto.response;

import java.util.Date;

public interface PostSummary {
    Integer getId();
    String getTitle();
    Date getDate();
    UserSummary getUser();
}
