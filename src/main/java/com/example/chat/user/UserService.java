package com.example.chat.user;

import com.example.chat.security.TokenProvider;
import com.example.chat.user.dto.TokenDto;
import com.example.chat.user.dto.UserInfoResDto;
import com.example.chat.user.dto.UserLoginReqDto;
import com.example.chat.user.dto.UserSignupReqDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;
import java.time.Duration;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider, RedisTemplate<String, String> redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.redisTemplate = redisTemplate;
    }

    public UserInfoResDto SingUp(UserSignupReqDto request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }

        User user = request.toEntity();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return UserInfoResDto.fromEntity(user);
    }

    @Transactional
    public TokenDto Login(UserLoginReqDto request){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀립니다.");
        }

        String accessToken = tokenProvider.createAccessToken(user);

        return TokenDto.builder()
                .accessToken(accessToken)
                .build();
    }

    @Transactional
    public void logout(Principal principal, HttpServletRequest request) {
        if (principal == null) {
            throw new IllegalStateException("인증된 사용자가 존재하지 않습니다.");
        }

        String email = principal.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        String token = tokenProvider.resolveToken(request);
        long expirationTime = tokenProvider.getTokenExpiration(token);
        redisTemplate.opsForValue().set(token, "blacklisted", Duration.ofMillis(expirationTime));
    }

    @Transactional
    public void deleteUser(Principal principal, HttpServletRequest request) {
        if (principal == null) {
            throw new IllegalStateException("인증된 사용자가 존재하지 않습니다.");
        }

        String email = principal.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        String token = tokenProvider.resolveToken(request);
        redisTemplate.delete(token);

        userRepository.delete(user);
    }
}