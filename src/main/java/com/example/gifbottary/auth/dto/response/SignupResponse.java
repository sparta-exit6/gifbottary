package com.example.gifbottary.auth.dto.response;

import com.example.gifbottary.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {

    private Long userId;
    private String email;
    private String nickName;

    public static SignupResponse from(User user) {
        return SignupResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickName(user.getNickname())
                .build();
    }

}
