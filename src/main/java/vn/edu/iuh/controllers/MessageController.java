package vn.edu.iuh.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.dto.MessageDTO;
import vn.edu.iuh.models.ChatMessage;
import vn.edu.iuh.models.Message;
import vn.edu.iuh.models.Notification;
import vn.edu.iuh.models.enums.MessageStatus;
import vn.edu.iuh.models.enums.MessageType;
import vn.edu.iuh.models.enums.NotificationType;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.services.ChatService;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;
    @MessageMapping("/chat/{chat-id}")
    public Message receivePublicMessage(@Payload MessageDTO messageDTO, @DestinationVariable("chat-id") String chatId) {
        log.info(messageDTO.toString());
        Message message = chatService.saveMessage(messageDTO, chatId);
        simpMessagingTemplate.convertAndSendToUser(chatId,"/private", message);
        return message;
    }
}
