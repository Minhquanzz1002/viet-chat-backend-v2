package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
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
import vn.edu.iuh.dto.MessageDTO;
import vn.edu.iuh.models.ChatMessage;
import vn.edu.iuh.models.Notification;
import vn.edu.iuh.models.enums.NotificationType;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/message/public")
    @SendTo("/chatroom/public")
    public ChatMessage receivePublicMessage(@Payload ChatMessage chatMessage) {
        log.info("public chat");
        log.info(chatMessage.toString());
        return chatMessage;
    }

    @MessageMapping("/private-message")
    public ChatMessage addUser(@Payload ChatMessage chatMessage) {
        log.info("private chat");
        log.info(chatMessage.toString());
        simpMessagingTemplate.convertAndSendToUser(chatMessage.getReceiver(), "/private", chatMessage);
        return chatMessage;
    }

    @MessageMapping("/private-message/{receiver-id}")
    public Notification sendPrivateMessage(@Payload MessageDTO messageDTO, @DestinationVariable("receiver-id") String receiverId) {
        log.info(messageDTO.toString());
        Notification notification = new Notification("Bạn nhận được một tin nhắn mới", NotificationType.NEW_MESSAGE, messageDTO.getSender(), LocalDateTime.now());
        simpMessagingTemplate.convertAndSendToUser(receiverId, "/private", notification);
        return notification;
    }

    public Notification sendPublicMessage() {
        return null;
    }
}
