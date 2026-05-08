package io.kemalthes.semesterwork3.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class AuthenticationHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        try {
            if (userId != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UUID.fromString(userId);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (IllegalArgumentException ignored) {
                    SecurityContextHolder.clearContext();
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
