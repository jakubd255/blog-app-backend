package pl.jakubdudek.blogappbackend.model.dto.response;

import java.util.Date;

public record PostDto(
        Integer id,
        String title,
        String body,
        Date date,
        UserDto user
) {
}
