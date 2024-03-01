package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.services.ChatService;

@RestController
@RequestMapping("/v1/chats")
@Tag(name = "Chats Controller", description = "Quản lý chat")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @Operation(summary = "Gửi tin nhắn")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createChat() {

    }

    @Operation(summary = "Lấy tin nhắn theo ID")
    @GetMapping("/{id}")
    public void getChat(@PathVariable String id) {

    }

    @Operation(summary = "Lấy tất cả tin nhắn")
    @GetMapping
    public void getAllChat() {

    }
}
