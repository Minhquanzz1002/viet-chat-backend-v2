package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.dto.MessageRequestDTO;
import vn.edu.iuh.models.Chat;
import vn.edu.iuh.models.Message;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.ChatService;

import java.util.List;

@RestController
@RequestMapping("/v1/chats")
@Tag(name = "Chats Controller", description = "Quản lý chat")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ChatService chatService;

    @Operation(
            summary = "Gửi tin nhắn mới",
            description = """
                    
                    """
    )
    @PostMapping("/{chat-id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public Message sendMessage(@PathVariable("chat-id") String chatId, @ModelAttribute MessageRequestDTO messageRequestDTO, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return chatService.sendMessage(messageRequestDTO, chatId, userPrincipal);
    }

    @Operation(
            summary = "Lấy tất cả tin nhắn của phòng chat",
            description = """
                    Lấy toàn bộ tin nhắn của phòng chat theo ID. Trả về danh sách tin nhắn và thông tin người gửi phục vụ cho render tin nhắn.\n
                    <strong>Lỗi nếu: </strong> không tìm thấy phòng chat bằng ID
                    """
    )
    @GetMapping("/{chat-id}/messages")
    public List<Message> getAllChat(@PathVariable("chat-id") String chatId) {
        return chatService.getAllMessages(chatId);
    }

    @Operation(
            summary = "Lấy thông tin nhóm chat theo ID",
            description = """
                    Lấy thông tin nhóm chat theo ID\n
                    <strong>Lỗi nếu: </strong> không tìm thấy phòng chat
                    """
    )
    @GetMapping("/{chat-id}")
    public Chat getChat(@PathVariable("chat-id") String id) {
        return chatService.findById(id);
    }
}
