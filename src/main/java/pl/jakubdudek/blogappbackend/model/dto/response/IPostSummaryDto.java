package pl.jakubdudek.blogappbackend.model.dto.response;

import pl.jakubdudek.blogappbackend.model.enums.PostStatus;

import java.util.Date;

public interface IPostSummaryDto {
    Integer getId();
    String getTitle();
    Date getDate();
    IUserDto getUser();
    PostStatus getStatus();
    Long getLikes();
    Long getComments();
}
