package pl.jakubdudek.blogappbackend.model.dto.response;

import java.util.Date;

public record PostResponse(
        Integer id,
        String title,
        String body,
        Date date,
        UserResponse user
) {
}
