package com.linked.classbridge.domain;

import com.linked.classbridge.type.AuthType;
import com.linked.classbridge.type.Gender;
import com.linked.classbridge.type.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
@Table(name = "`user`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
@SQLDelete(sql = "UPDATE user SET deleted_at = NOW() WHERE user_id = ?")
@SQLRestriction("deleted_at is null")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    @Email(message = "유효한 이메일 주소 형식이어야 합니다.")
    private String email;

    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthType authType;

    private String provider;

    private String providerId;

    private String loginId;

    @Column(nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "생년월일은 yyyy-mm-dd 형식이어야 합니다.")
    private String birthDate;

    private String age;

    @Column(nullable = false)
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-xxxx-xxxx 형식이어야 합니다.")
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

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviewList;
}
