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
            + "JOIN FETCH cr.initiatedBy "
            + "JOIN FETCH cr.initiatedTo "
            + "where cr.initiatedBy = :user or cr.initiatedTo = :user "
            + "order by cr.updatedAt desc")
    List<ChatRoom> findAllByUserOrderByUpdatedAtDesc(User user);
}
