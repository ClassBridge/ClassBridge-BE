package com.linked.classbridge.domain;

import com.linked.classbridge.type.AuthType;
import com.linked.classbridge.type.Gender;
import com.linked.classbridge.type.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
@SQLDelete(sql = "UPDATE user SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at is null")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthType authType;

    private String provider;

    private String providerId;

    private String loginId;

    @Column(nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String nickname;

    private Gender gender;

    private String birthDate;

    private String age;

    @Column(nullable = false)
    private String phone;

    private String profileImageUrl;

    private String interests; // 카테고리 테이블 추가 시 수정

    private String selfIntroduction;

    private String businessRegistrationNumber;

    private String bankName;

    private String accountNumber;

    @ElementCollection(targetClass = UserRole.class)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Fetch(FetchMode.JOIN) // 즉시로딩 설정
    private List<UserRole> roles;

    private LocalDateTime deletedAt;
}
