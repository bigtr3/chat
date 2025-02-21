package com.example.chat.user.dto;

import com.example.chat.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserSignupReqDto {
    private String email;
    private String phoneNumber;
    private String password;
    private String name;

    public User toEntity() {
        return User.builder()
                .email(this.email)
                .phoneNumber(this.phoneNumber)
                .name(this.name)
                .build();
    }
}