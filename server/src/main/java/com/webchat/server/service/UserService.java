package com.webchat.server.service;

import com.webchat.server.entity.User;
import com.webchat.server.model.UserDTO;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    Optional<User> getUserEntityById(UUID id);

    Optional<UserDTO> getUserDTOById(UUID id);

    Optional<UserDTO> getUserByUsername(String username);

    UserDTO register(UserDTO user);

    boolean checkPassword(UserDTO user, UserDTO savedUser);

    Optional<UserDTO> updatePassword(User user, String password);

    Optional<UserDTO> getUserById(UUID id);

    boolean doesUserExistsWithUsername(String username);
}
