package pl.jakubdudek.blogappbackend.model.dto.response;

import pl.jakubdudek.blogappbackend.model.enums.PostStatus;

import java.util.Date;

public interface IPostDto {
    Integer getId();
    String getTitle();
    String getBody();
    Date getDate();
    IUserDto getUser();
    PostStatus getStatus();
    Long getLikes();
    Long getComments();
    Boolean getIsLiked();
}
