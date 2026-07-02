package com.example.gifbottary.domain.chat.dto;

import java.security.Principal;

public record StompPrincipal(Long userId, String userName) implements Principal {
    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
