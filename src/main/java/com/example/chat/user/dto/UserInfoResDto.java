package com.example.chat.user.dto;

import com.example.chat.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserInfoResDto {
    private Long id;
    private String email;
    private String nickname;
    private String phoneNumber;
    private String name;

    public static UserInfoResDto fromEntity(User user) {
        return UserInfoResDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .name(user.getName())
                .build();
    }
}
