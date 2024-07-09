package pl.jakubdudek.blogappbackend.util.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.UnknownContentTypeException;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import pl.jakubdudek.blogappbackend.service.AuthenticationService;
import pl.jakubdudek.blogappbackend.util.cookie.CookieManager;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtGenerator jwtGenerator;
    private final JwtExtractor jwtExtractor;
    private final CookieManager cookieManager;
    private final AuthenticationService authenticationService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String email;
        String token = jwtExtractor.extractJwt(request);

        if(token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            email = jwtGenerator.extractUsername(token);
        }
        catch(ExpiredJwtException e) {
            returnResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT expired");
            return;
        }
        catch(MalformedJwtException | UnsupportedJwtException | IllegalArgumentException | UnknownContentTypeException e) {
            returnResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT");
            return;
        }

        if(!email.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = authenticationService.userDetailsService().loadUserByUsername(email);

                if(jwtGenerator.isTokenValid(token, userDetails.getUsername())) {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authToken);
                    SecurityContextHolder.setContext(context);
                }
            }
            catch(UsernameNotFoundException e) {
                returnResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void returnResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write(message);
        cookieManager.removeCookies(response);
    }
}
