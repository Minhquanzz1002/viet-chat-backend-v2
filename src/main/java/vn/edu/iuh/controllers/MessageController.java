package vn.edu.iuh.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.models.ChatMessage;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    public ChatMessage receivePublicMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    @MessageMapping("/private-message")
    @SendTo("/user/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage) {
        simpMessagingTemplate.convertAndSendToUser(chatMessage.getReceiverName(), "/private", chatMessage);
        return chatMessage;
    }
}
