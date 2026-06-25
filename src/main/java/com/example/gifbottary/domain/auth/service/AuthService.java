package com.example.gifbottary.domain.auth.service;

import com.example.gifbottary.domain.auth.dto.request.LoginRequest;
import com.example.gifbottary.domain.auth.dto.request.SignupRequest;
import com.example.gifbottary.domain.auth.dto.response.LoginResponse;
import com.example.gifbottary.domain.auth.dto.response.SignupResponse;
import com.example.gifbottary.domain.auth.jwt.JwtProvider;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        try {
            User user = request.toEntity(passwordEncoder);

            User savedUser = userRepository.save(user);

            return SignupResponse.from(savedUser);

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user);

        return LoginResponse.from(accessToken);
    }
}
