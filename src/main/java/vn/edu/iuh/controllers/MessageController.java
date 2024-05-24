package vn.edu.iuh.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.dto.MessageDTO;
import vn.edu.iuh.dto.MessageEventDTO;
import vn.edu.iuh.models.Message;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.ChatService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/{chat-id}/seen")
    public void sendMessage(@Payload String senderId, @DestinationVariable("chat-id") String chatId) {
        chatService.seenMessage(chatId, senderId);
    }

    @MessageMapping("/chat/{chat-id}")
    public Message sendMessage(@Payload MessageDTO messageDTO, @DestinationVariable("chat-id") String chatId) {
        Message message = chatService.saveMessage(messageDTO, chatId);
        simpMessagingTemplate.convertAndSend("/chatroom/" + chatId, message);
        return message;
    }

    @MessageMapping("/chat/{chat-id}/delete")
    public void deleteMessage(@DestinationVariable("chat-id") String chatId, @Payload MessageEventDTO messageEventDTO) {
        chatService.deleteMessage(messageEventDTO, chatId);
    }

    @MessageMapping("/chat/{chat-id}/unsend")
    public void unsendMessage(@DestinationVariable("chat-id") String chatId, @Payload MessageEventDTO messageEventDTO) {
        chatService.unsendMessage(messageEventDTO, chatId);
    }
}
