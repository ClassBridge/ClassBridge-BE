package com.linked.classbridge.dto.user;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.type.Gender;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoDto {
    private Long userId;
    private String email;
    private String userName;
    private String nickname;
    private Gender gender;
    private String birthDate;
    private String phone;
    private String profileImageUrl;

    private List<String> interests;
    private String selfIntroduction;
    private String businessRegistration;
    private String bankName;
    private String accountNumber;



    public UserInfoDto(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.userName = user.getUsername() == null ? "" : user.getUsername();
        this.nickname = user.getNickname();
        this.gender = user.getGender() == null ? null : user.getGender();
        this.birthDate = user.getBirthDate() == null ? "" : user.getBirthDate();
        this.phone = user.getPhone();
        this.profileImageUrl = user.getProfileImageUrl() == null ? "" : user.getProfileImageUrl();
        this.interests = user.getInterests().stream().map(item -> item.getName().toString()).toList();
        this.selfIntroduction = user.getSelfIntroduction() == null ? "" : user.getSelfIntroduction();
        this.businessRegistration = user.getBusinessRegistrationNumber() == null ? "" : user.getBusinessRegistrationNumber();
        this.bankName = user.getBankName() == null ? "" : user.getBankName();
        this.accountNumber = user.getAccountNumber() == null ? "" : user.getAccountNumber();
    }

}
