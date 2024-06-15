package com.linked.classbridge.service;

import com.linked.classbridge.repository.UserChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserChatRoomService {

    private final UserService userService;

    private final UserChatRoomRepository userChatRoomRepository;

    public void leaveAllChatRoomsByEmail(String email) {
        log.info("Disconnecting all chat rooms for user: {}", email);
        userService.findByEmail(email)
                .ifPresent(user -> userChatRoomRepository.updateIsOnlineByUser_Email(email, false));
    }

}
