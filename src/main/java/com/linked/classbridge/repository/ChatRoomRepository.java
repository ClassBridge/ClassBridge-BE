package com.linked.classbridge.repository;

import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByInitiatedByAndInitiatedTo(User initiatedBy, User initiatedTo);

    @Query("select cr from ChatRoom cr "
            + "JOIN FETCH cr.userChatRooms "
            + "where cr.chatRoomId = :chatRoomId and cr.deletedAt is null")
    Optional<ChatRoom> findByChatRoomId(Long chatRoomId);

    @Query("SELECT DISTINCT cr FROM UserChatRoom ucr "
            + "JOIN ucr.chatRoom cr "
            + "JOIN FETCH cr.initiatedBy "
            + "JOIN FETCH cr.initiatedTo "
            + "WHERE ucr.user = :user "
            + "AND ucr.deletedAt IS NULL "
            + "AND cr.deletedAt IS NULL "
            + "ORDER BY cr.updatedAt DESC")
    List<ChatRoom> findAllByUserOrderByLastMessageAtDesc(User user);
}
