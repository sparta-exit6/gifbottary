package com.example.gifbottary.domain.user.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    private int pointBalance;

    // 테스트를 위한 임시 생성자
    public User(String email, String password, String name, String role, int pointBalance) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.pointBalance = pointBalance;
    }
}