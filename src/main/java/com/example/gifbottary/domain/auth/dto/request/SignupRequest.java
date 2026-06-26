package com.example.gifbottary.domain.auth.dto.request;

import com.example.gifbottary.domain.user.entity.Role;
import com.example.gifbottary.domain.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
public class SignupRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    private String name;

    public User toEntity(PasswordEncoder passwordEncoder) {

        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .role(Role.USER)          // 항상 USER(관리자는 회원가입 x)
                .pointBalance(0)
                .build();
    }
}
