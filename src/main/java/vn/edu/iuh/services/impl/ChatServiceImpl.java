package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.MessageRequestDTO;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.exceptions.UserNotInChatException;
import vn.edu.iuh.models.*;
import vn.edu.iuh.models.enums.AttachmentType;
import vn.edu.iuh.models.enums.MessageStatus;
import vn.edu.iuh.models.enums.NotificationType;
import vn.edu.iuh.repositories.ChatRepository;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.repositories.UserRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.ChatService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final UserInfoRepository userInfoRepository;
    private final S3Service s3Service;
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
    public Message sendMessage(MessageRequestDTO messageRequestDTO, String chatId, UserPrincipal sender) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        UserInfo senderUserInfo = userInfoRepository.findByUser(new User(sender.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        if (!chat.getMembers().contains(senderUserInfo)) {
            throw new UserNotInChatException("Bạn không phải là thành viên nên không được phép gửi tin nhắn");
        }
        Message message;
        if (messageRequestDTO.getReplyMessageId() == null) {
            message = new Message(senderUserInfo, messageRequestDTO.getContent(), new ArrayList<>(), null, MessageStatus.SENT);
        } else {
            message = new Message(messageRequestDTO.getReplyMessageId(), senderUserInfo, messageRequestDTO.getContent(), new ArrayList<>(), null, MessageStatus.SENT);
        }

        if (messageRequestDTO.getFiles() != null) {
            messageRequestDTO.getFiles().forEach(file -> {
                String linkFile = s3Service.uploadFile(file, file.getOriginalFilename(), "chats/" + message.getMessageId());
                message.getAttachments().add(new Attachment(AttachmentType.IMAGE, linkFile, file.getOriginalFilename()));
            });
        }

        chat.getMessages().add(message);
        chatRepository.save(chat);
        chat.getMembers().forEach((member)-> {
            simpMessagingTemplate.convertAndSendToUser(member.getId(), "/private", new Notification("Bạn đã nhận được một tin nhắn mới từ " + senderUserInfo.getLastName(), NotificationType.NEW_MESSAGE, senderUserInfo.getId(), LocalDateTime.now()));
        });
        return message;
    }
}
