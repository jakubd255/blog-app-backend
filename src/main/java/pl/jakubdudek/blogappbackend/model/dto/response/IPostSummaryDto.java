package pl.jakubdudek.blogappbackend.model.dto.response;

import pl.jakubdudek.blogappbackend.model.enumerate.PostStatus;

import java.util.Date;

public interface IPostSummaryDto {
    Integer getId();
    String getTitle();
    Date getDate();
    IUserDto getUser();
    PostStatus getStatus();
}
