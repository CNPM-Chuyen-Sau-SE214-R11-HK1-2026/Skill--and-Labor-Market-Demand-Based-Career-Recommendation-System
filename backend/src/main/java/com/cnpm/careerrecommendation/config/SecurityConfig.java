package com.cnpm.careerrecommendation.config;

import com.cnpm.careerrecommendation.common.dto.ErrorResponse;
import com.cnpm.careerrecommendation.common.security.JwtAuthenticationFilter;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.cnpm.careerrecommendation.common.security.SecurityErrorHandler;
import com.cnpm.careerrecommendation.module.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;

/**
 * Baseline Spring Security configuration establishing stateless security filter chain,
 * public endpoint access (such as /api/health), CORS integration, and standardized error responses.
 * Enforces method-level security and protects all authenticated resources.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CorsProperties corsProperties;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private JwtTokenProvider jwtTokenProvider;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private SecurityErrorHandler securityErrorHandler;

    public SecurityConfig(CorsProperties corsProperties, ObjectMapper objectMapper) {
        this.corsProperties = corsProperties;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Health check endpoint
                        .requestMatchers("/api/health").permitAll()
                        // Public auth endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                        // Public taxonomy GETs from docs/api/taxonomy.md
                        .requestMatchers(HttpMethod.GET, "/api/v1/occupations/**", "/api/v1/skills", "/api/v1/skills/**").permitAll()
                        // All other endpoints require authentication (/api/v1/auth/me, /api/v1/profile/**, etc.)
                        .anyRequest().authenticated()
                );

        if (jwtTokenProvider != null && userRepository != null && securityErrorHandler != null) {
            JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(
                    jwtTokenProvider, userRepository, securityErrorHandler);
            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("WWW-Authenticate", "Bearer");

            ErrorResponse errorResponse = ErrorResponse.of(
                    HttpStatus.UNAUTHORIZED,
                    "Yêu cầu xác thực tài khoản để truy cập tài nguyên này",
                    request.getRequestURI()
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            ErrorResponse errorResponse = ErrorResponse.of(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền truy cập tài nguyên này",
                    request.getRequestURI()
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
