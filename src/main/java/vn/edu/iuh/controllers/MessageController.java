package vn.edu.iuh.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.dto.MessageDTO;
import vn.edu.iuh.models.Message;
import vn.edu.iuh.services.ChatService;

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
        simpMessagingTemplate.convertAndSend("/chatroom/" + chatId, message);
        return message;
    }
}
