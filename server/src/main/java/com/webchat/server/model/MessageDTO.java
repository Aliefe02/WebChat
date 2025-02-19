package com.webchat.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class MessageDTO {
    private UUID senderId;

    private UUID receiverId;

    private String message;

    private LocalDateTime sentAt;
}
