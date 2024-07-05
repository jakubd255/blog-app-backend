package pl.jakubdudek.blogappbackend.util.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieManager {
    @Value("${token.expiration.time}")
    private int jwtExpirationTime;

    public void addCookie(HttpServletResponse response, String token) {
        //HTTP-Only cookie with JWT
        Cookie tokenCookie = createCookie("access-token", token, true, jwtExpirationTime);
        //Cookie for JavaScript to check is user logged-in
        Cookie isLoggedCookie = createCookie("is-logged", "true", false, jwtExpirationTime);

        response.addCookie(tokenCookie);
        response.addCookie(isLoggedCookie);
    }

    public void removeCookies(HttpServletResponse response) {
        Cookie tokenCookie = createCookie("access-token", null, true, 0);

        Cookie isLoggedCookie = createCookie("is-logged", null, false, 0);

        response.addCookie(tokenCookie);
        response.addCookie(isLoggedCookie);
    }

    private Cookie createCookie(String name, String value, boolean isHttpOnly, int maxAge) {
        Cookie  cookie = new Cookie(name, value);
        cookie.setHttpOnly(isHttpOnly);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        return cookie;
    }
}
