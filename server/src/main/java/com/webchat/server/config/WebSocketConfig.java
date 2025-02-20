package com.webchat.server.config;

import com.webchat.server.handler.ChatWebSocketHandler;
import com.webchat.server.interceptor.AuthHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AuthHandshakeInterceptor authHandshakeInterceptor;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public WebSocketConfig(AuthHandshakeInterceptor authHandshakeInterceptor, ChatWebSocketHandler chatWebSocketHandler) {
        this.authHandshakeInterceptor = authHandshakeInterceptor;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(authHandshakeInterceptor) // Add the interceptor here
                .setAllowedOrigins("*");  // Adjust as needed
    }
}

