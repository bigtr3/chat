package com.example.chat.chat;

import com.example.chat.user.User;
import com.example.chat.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
public class ChatController {
    private final SimpMessageSendingOperations messagingTemplate;
    private final UserRepository userRepository;  // Inject UserRepository

    public ChatController(SimpMessageSendingOperations messagingTemplate, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)  // Fetch User entity
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        chatMessage.setSender(user.getName());
        messagingTemplate.convertAndSend("/topic/room-" + chatMessage.getRoom(), chatMessage);
        log.info("Message sent by {} to room {}: {}", user.getName(), chatMessage.getRoom(), chatMessage.getContent());
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)  // Fetch User entity
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        chatMessage.setSender(user.getName()); // Use Name Instead of Email
        headerAccessor.getSessionAttributes().put("username", user.getName()); // Store name instead of email
        headerAccessor.getSessionAttributes().put("room", chatMessage.getRoom());

        messagingTemplate.convertAndSend("/topic/room-" + chatMessage.getRoom(), chatMessage);
        log.info("User {} joined room {}", user.getName(), chatMessage.getRoom());
    }
}

