package com.webchat.server.controller;

import com.webchat.server.entity.User;
import com.webchat.server.exception.NotFoundException;
import com.webchat.server.exception.UnauthorizedException;
import com.webchat.server.model.UserDTO;
import com.webchat.server.security.JWTUtil;
import com.webchat.server.security.SecurityUtils;
import com.webchat.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user/")
public class UserController {

    private final UserService userService;
    private final JWTUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public static User getUserFromToken(UserService userService){
        return userService.getUserEntityById(SecurityUtils.getAuthenticatedUserId()).orElseThrow(UnauthorizedException::new);
    }

    public static UserDTO getUserDTOFromToken(UserService userService){
        return userService.getUserById(SecurityUtils.getAuthenticatedUserId()).orElseThrow(NotFoundException::new);
    }

    @PostMapping("login")
    public String login(@RequestBody UserDTO userDTO){
        UserDTO user = userService.getUserByUsername(userDTO.getUsername()).orElseThrow(() -> new BadCredentialsException("User not found"));

        if (userService.checkPassword(userDTO, user)){
            return jwtUtil.generateToken(user.getId());
        }
        throw new BadCredentialsException("Password incorrect");
    }

    @PostMapping("register")
    public ResponseEntity<String> register(@Validated @RequestBody UserDTO userDTO){
        UserDTO savedUser = userService.register(userDTO);

        String token = jwtUtil.generateToken(savedUser.getId());

        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }

    @PutMapping("update-password")
    public ResponseEntity<Void> updatePassword(@RequestBody UserDTO userDTO){
        User user = getUserFromToken(userService);

        userService.updatePassword(user, userDTO.getPassword()).orElseThrow(NotFoundException::new);

        return new ResponseEntity<>(HttpStatus.OK);
        }

    @GetMapping("details")
    public UserDTO getUserDetails(){
        UserDTO userDTO = getUserDTOFromToken(userService);
        userDTO.setPassword(null);
        return userDTO;
    }

    @GetMapping("exists")
    public ResponseEntity<Void> doesUserExists(@RequestParam("username") String username){
        if (userService.doesUserExistsWithUsername(username)){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }





}
