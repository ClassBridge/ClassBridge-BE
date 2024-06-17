package com.linked.classbridge.repository;

import com.linked.classbridge.domain.UserChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {

    @Modifying
    @Query("UPDATE UserChatRoom ucr SET ucr.isOnline = ?2 WHERE ucr.user = (SELECT u FROM User u WHERE u.email = ?1)")
    void updateIsOnlineByUser_Email(String userEmail, boolean isOnline);

    @Query("select ucr from UserChatRoom ucr "
            + "JOIN FETCH ucr.user "
            + "where ucr.chatRoom.chatRoomId = :chatRoomId ")
    List<UserChatRoom> findAllByChatRoomIdIncludingDeleted(Long chatRoomId);
}
