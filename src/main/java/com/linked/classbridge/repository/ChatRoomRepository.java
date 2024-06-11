package com.linked.classbridge.repository;

import com.linked.classbridge.domain.ChatRoom;
import com.linked.classbridge.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByInitiatedByAndInitiatedTo(User initiatedBy, User initiatedTo);
}
