package com.webchat.server.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.UUID;

@Getter
@Setter
public class CustomAuthenticationToken extends AbstractAuthenticationToken {

    private final UUID userId;

    public CustomAuthenticationToken(UUID userId) {
        super(null);
        this.userId = userId;
        setAuthenticated(true);  // Token is considered authenticated if valid
    }

    @Override
    public Object getCredentials() {
        return null;  // No password needed in the token
    }

    @Override
    public Object getPrincipal() {
        return userId;  // The user ID is the principal
    }
}
