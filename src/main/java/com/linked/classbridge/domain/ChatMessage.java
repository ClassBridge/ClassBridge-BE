package com.linked.classbridge.domain;

import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat_message")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChatMessage {

    @Id
    private String id;

    private Long chatRoomId;

    private Long senderId;

    private String message;

    private LocalDateTime sendTime;

    private boolean isRead;

    public void readMessage() {
        this.isRead = true;
    }
}
