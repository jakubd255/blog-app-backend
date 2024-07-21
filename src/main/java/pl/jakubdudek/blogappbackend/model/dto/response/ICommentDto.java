package pl.jakubdudek.blogappbackend.model.dto.response;

import java.util.Date;

public interface ICommentDto {
    Integer getId();
    String getText();
    IUserDto getUser();
    Date getDate();
    Integer getPostId();
    Integer getParentId();
    Long getLikes();
    Long getReplies();
}
