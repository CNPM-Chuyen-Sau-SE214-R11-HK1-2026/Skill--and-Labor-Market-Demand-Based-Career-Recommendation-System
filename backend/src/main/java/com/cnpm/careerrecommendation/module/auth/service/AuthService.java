package com.cnpm.careerrecommendation.module.auth.service;

import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.security.AuthenticatedUser;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.cnpm.careerrecommendation.module.auth.dto.*;
import com.cnpm.careerrecommendation.module.auth.entity.User;
import com.cnpm.careerrecommendation.module.auth.repository.UserRepository;
import java.sql.SQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final AuthenticationManager authentication;
    private final JwtTokenProvider tokens;

    public AuthService(UserRepository users, PasswordEncoder passwords,
                       AuthenticationManager authentication, JwtTokenProvider tokens) {
        this.users = users;
        this.passwords = passwords;
        this.authentication = authentication;
        this.tokens = tokens;
    }

    @Transactional
    public RegisteredUserResponse register(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) { throw emailConflict(); }
        User user = User.candidate(request.email(), passwords.encode(request.password()),
                request.fullName(), request.phone());
        try {
            return RegisteredUserResponse.from(users.saveAndFlush(user));
        } catch (DataIntegrityViolationException ex) {
            // Enforce uniqueness even for concurrent registrations. Do not expose SQL or hash values.
            if (ex.getMostSpecificCause() instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                throw emailConflict();
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        authentication.authenticate(UsernamePasswordAuthenticationToken
                .unauthenticated(request.email(), request.password()));
        User user = users.findByEmailIgnoreCase(request.email())
                .filter(User::isActive).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        return new LoginResponse(tokens.generateToken(user), "Bearer", tokens.getExpiresInSeconds(),
                LoginResponse.LoginUser.from(user));
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(AuthenticatedUser identity) {
        return users.findById(identity.userId()).filter(User::isActive)
                .map(CurrentUserResponse::from)
                .orElseThrow(() -> new BadCredentialsException("Invalid user"));
    }

    private ApiException emailConflict() {
        return new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email đã được đăng ký");
    }
}
