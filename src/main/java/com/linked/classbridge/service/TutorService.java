package com.linked.classbridge.service;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.tutor.TutorInfoDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.ErrorCode;
import com.linked.classbridge.type.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TutorService {

    private final UserService userService;
    private final UserRepository userRepository;

    public TutorService(UserService userService, UserRepository userRepository) {

        this.userService = userService;
        this.userRepository = userRepository;
    }

    public String registerTutor(TutorInfoDto tutorInfoDto) {

        log.info("registering tutor");

        User user = userRepository.findByEmail(userService.getCurrentUserEmail())
                .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));

        if (user.getRoles().contains(UserRole.ROLE_TUTOR)) {
            throw new RestApiException(ErrorCode.ALREADY_REGISTERED_TUTOR);
        }

        if (tutorInfoDto.getBank() == null || tutorInfoDto.getAccount() == null) {
            throw new RestApiException(ErrorCode.MISSING_BANK_ACCOUNT_INFO);
        }

        user.getRoles().add(UserRole.ROLE_TUTOR);
        user.setBankName(tutorInfoDto.getBank());
        user.setAccountNumber(tutorInfoDto.getAccount());
        user.setBusinessRegistrationNumber(tutorInfoDto.getBusinessRegistrationNumber());
        user.setSelfIntroduction(tutorInfoDto.getIntroduction());
        userRepository.save(user);

        log.info("tutor registered successfully");

        return "tutor registered successfully";
    }

    public String updateTutorInfo(TutorInfoDto tutorInfoDto){

        log.info("updating tutor info");

        User user = userRepository.findByEmail(userService.getCurrentUserEmail())
                .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));

        if(!user.getRoles().contains(UserRole.ROLE_TUTOR)){
            throw new RestApiException(ErrorCode.NOT_REGISTERED_TUTOR);
        }

        user.setBankName(tutorInfoDto.getBank() != null ? tutorInfoDto.getBank() : user.getBankName());
        user.setAccountNumber(tutorInfoDto.getAccount() != null ? tutorInfoDto.getAccount() : user.getAccountNumber());
        user.setBusinessRegistrationNumber(tutorInfoDto.getBusinessRegistrationNumber() != null ?
                tutorInfoDto.getBusinessRegistrationNumber() : user.getBusinessRegistrationNumber());
        user.setSelfIntroduction(tutorInfoDto.getIntroduction() != null ? tutorInfoDto.getIntroduction() : user.getSelfIntroduction());
        userRepository.save(user);

        log.info("tutor info updated successfully");

        return "tutor info updated successfully";
    }
}
