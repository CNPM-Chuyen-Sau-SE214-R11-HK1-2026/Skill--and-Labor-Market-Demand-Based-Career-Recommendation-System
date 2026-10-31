package com.cnpm.careerrecommendation.common.security;

import com.cnpm.careerrecommendation.module.auth.entity.User;
import com.cnpm.careerrecommendation.module.auth.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokens;
    private final UserRepository users;
    private final SecurityErrorHandler errors;

    public JwtAuthenticationFilter(JwtTokenProvider tokens, UserRepository users, SecurityErrorHandler errors) {
        this.tokens = tokens;
        this.users = users;
        this.errors = errors;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "POST".equals(request.getMethod()) && ("/api/v1/auth/login".equals(request.getServletPath())
                || "/api/v1/auth/register".equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        List<String> headers = Collections.list(request.getHeaders(HttpHeaders.AUTHORIZATION));
        if (headers.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }
        try {
            String header = headers.get(0);
            if (headers.size() != 1 || !header.regionMatches(true, 0, "Bearer ", 0, 7)
                    || header.length() <= 7) {
                throw new BadCredentialsException("Invalid authorization header");
            }
            AuthenticatedUser identity = tokens.parseToken(header.substring(7));
            User user = users.findById(identity.userId())
                    .orElseThrow(() -> new BadCredentialsException("Invalid user"));
            // Database is authoritative: disabling or changing a role invalidates old tokens immediately.
            if (!user.isActive() || !user.getEmail().equals(identity.email()) || user.getRole() != identity.role()) {
                throw new BadCredentialsException("Stale identity");
            }
            var authentication = new UsernamePasswordAuthenticationToken(identity, null,
                    List.of(new SimpleGrantedAuthority(user.getRole().name())));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        } catch (JwtException | IllegalArgumentException | BadCredentialsException ex) {
            SecurityContextHolder.clearContext();
            errors.commence(request, response, new BadCredentialsException("Invalid bearer token"));
            return;
        }
        chain.doFilter(request, response);
    }
}
