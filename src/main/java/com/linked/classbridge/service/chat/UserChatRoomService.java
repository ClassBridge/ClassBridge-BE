package com.linked.classbridge.service.chat;

import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserChatRoom;
import com.linked.classbridge.repository.UserChatRoomRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserChatRoomService {

    private final UserChatRoomRepository userChatRoomRepository;

    private final EntityManager entityManager;


    public void saveUserChatRoom(UserChatRoom userChatRoom) {
        userChatRoomRepository.save(userChatRoom);
    }

    public UserChatRoom deleteUserChatRoom(UserChatRoom userChatRoom) {
        userChatRoomRepository.delete(userChatRoom);
        return userChatRoom;
    }

    public void leaveAllChatRoomsByEmail(User user) {
        log.info("Disconnecting all chat rooms for user: {}", user.getEmail());
        userChatRoomRepository.updateIsOnlineByUser_Email(user.getEmail(), false);
    }

    protected List<UserChatRoom> findAllUserChatRoomsByChatRoomIncludingDeleted(ChatRoom chatRoom) {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("deletedChatRoomFilter");
        List<UserChatRoom> userChatRooms =
                userChatRoomRepository.findAllByChatRoomIdIncludingDeleted(chatRoom.getChatRoomId());
        session.enableFilter("deletedChatRoomFilter");
        return userChatRooms;
    }

    protected void setUserToOnline(User user, List<UserChatRoom> userChatRooms) {
        userChatRooms.stream()
                .filter(userChatRoom -> userChatRoom.getUser().getUserId().equals(user.getUserId()))
                .findFirst()
                .ifPresent(userChatRoom -> {
                    userChatRoom.setOnline();
                    saveUserChatRoom(userChatRoom);
                });
    }


    public void setUserToOffline(User user, List<UserChatRoom> userChatRooms) {
        userChatRooms.stream()
                .filter(userChatRoom -> userChatRoom.getUser().getUserId().equals(user.getUserId()))
                .findFirst()
                .ifPresent(userChatRoom -> {
                    userChatRoom.setOffline();
                    saveUserChatRoom(userChatRoom);
                });
    }
}
