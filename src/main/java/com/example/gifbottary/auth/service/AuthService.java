package com.example.gifbottary.auth.service;

import com.example.gifbottary.auth.dto.request.SignupRequest;
import com.example.gifbottary.auth.dto.response.SignupResponse;
import com.example.gifbottary.user.entity.User;
import com.example.gifbottary.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = request.toEntity(passwordEncoder);
        User savedUser = userRepository.save(user);

        return SignupResponse.from(savedUser);
    }
}
