package com.webchat.server.mapper;

import com.webchat.server.entity.User;
import com.webchat.server.model.UserDTO;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    User userDTOToUser(UserDTO dto);

    UserDTO userToUserDTO(User user);
}
