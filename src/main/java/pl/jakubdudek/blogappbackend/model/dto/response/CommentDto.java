package pl.jakubdudek.blogappbackend.model.dto.response;

import java.util.Date;

public record CommentDto(
        Integer id,
        String text,
        UserDto user,
        Date date
) {
}
