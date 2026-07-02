package com.example.gifbottary.common.security.principal;

/**
 * 인증되지 않은 공개 요청에서도 @AuthenticationPrincipal(expression = "id") 를
 * 안전하게 사용할 수 있도록 id 필드만 제공하는 익명 principal 입니다.
 */
public class AnonymousPrincipal {

    public Long getId() {
        return null;
    }
}