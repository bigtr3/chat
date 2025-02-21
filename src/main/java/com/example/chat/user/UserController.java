package com.example.chat.user;

import com.example.chat.user.dto.TokenDto;
import com.example.chat.user.dto.UserInfoResDto;
import com.example.chat.user.dto.UserLoginReqDto;
import com.example.chat.user.dto.UserSignupReqDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserInfoResDto> SignUp(@RequestBody UserSignupReqDto request) {
        UserInfoResDto savedUser = userService.SingUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> Login(@RequestBody UserLoginReqDto request) {
        TokenDto tokenDto = userService.Login(request);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(tokenDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Principal principal, HttpServletRequest request) {
        userService.logout(principal, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deleteUser")
    public ResponseEntity<Void> deleteUser(Principal principal, HttpServletRequest request) {
        userService.deleteUser(principal, request);
        return ResponseEntity.ok().build();
    }
}