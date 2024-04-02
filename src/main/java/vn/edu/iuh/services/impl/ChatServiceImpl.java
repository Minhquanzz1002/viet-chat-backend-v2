package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.MessageDTO;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.models.Chat;
import vn.edu.iuh.models.Message;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.models.enums.MessageStatus;
import vn.edu.iuh.models.enums.MessageType;
import vn.edu.iuh.repositories.ChatRepository;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.services.ChatService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final UserInfoRepository userInfoRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public List<Message> getAllMessages(String chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        return chat.getMessages();
    }

    @Override
    public Chat findById(String id) {
        return chatRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + id));
    }

    @Override
    public Message saveMessage(MessageDTO messageDTO, String chatId) {
        UserInfo sender = userInfoRepository.findById(messageDTO.getSender()).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Message message = Message.builder()
                .messageId(new ObjectId())
                .replyMessageId(messageDTO.getReplyMessageId() != null ? new ObjectId(messageDTO.getReplyMessageId()) : null)
                .content(messageDTO.getContent())
                .attachments(messageDTO.getAttachments())
                .status(MessageStatus.SENT)
                .type(MessageType.MESSAGE)
                .sender(sender)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        chat.getMessages().add(message);
        chatRepository.save(chat);
        return message;
    }

}
