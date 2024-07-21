package pl.jakubdudek.blogappbackend.model.dto.response;

import pl.jakubdudek.blogappbackend.model.enums.PostStatus;

import java.util.Date;

public record PostDto(
        Integer id,
        String title,
        String body,
        PostStatus status,
        Date date,
        UserDto user
) {
}
