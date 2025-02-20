package com.webchat.server.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String username = (String) session.getAttributes().get("username"); // Assuming username is stored
        sessions.put(username, session);
        System.out.println(username + " connected");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String username = (String) session.getAttributes().get("username");
        System.out.println("Received from: " + username + " - " + message.getPayload());

        // ✅ Send message back to sender (Echo)
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));

        // ✅ Broadcast to all other users (Optional)
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen() && !s.equals(session)) {
                s.sendMessage(new TextMessage(username + ": " + message.getPayload()));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().remove(session);
        System.out.println("User disconnected");
    }
}
