package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.MessageDTO;
import vn.edu.iuh.dto.MessageEventDTO;
import vn.edu.iuh.dto.MessageRequestDTO;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.exceptions.MessageRecallTimeExpiredException;
import vn.edu.iuh.models.*;
import vn.edu.iuh.models.enums.MessageStatus;
import vn.edu.iuh.models.enums.MessageType;
import vn.edu.iuh.repositories.ChatRepository;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.ChatService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final UserInfoRepository userInfoRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public List<Message> getAllMessages(String chatId, UserPrincipal userPrincipal) {
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = findById(chatId);

        if (chat.getGroup() == null && !chat.getMembers().contains(userInfo) || chat.getGroup() != null && !chat.getGroup().getMembers().contains(GroupMember.builder().member(userInfo).build())) {
            throw new AccessDeniedException("Bạn không phải là thành viên của phòng chat này");
        }

        List<Message> messages = new ArrayList<>();
        chat.getMessages().forEach(
                message -> {
                    if (!message.getDeleteBy().contains(userInfo)) {
                        if (message.getStatus().equals(MessageStatus.UNSEND)) {
                            message.setContent("Tin nhắn đã bị thu hồi");
                            message.setAttachments(null);
                            message.setReactions(null);
                        }
                        messages.add(message);
                    }
                }
        );
        return messages;
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
        LastMessage lastMessage = LastMessage.builder()
                .createdAt(message.getCreatedAt())
                .sender(sender)
                .content(message.getContent() == null ? "[FILE]" : message.getContent())
                .build();
        chat.setLastMessage(lastMessage);
        chatRepository.save(chat);
        return message;
    }

    @Override
    public Message saveMessage(MessageRequestDTO messageRequestDTO, String chatId, UserPrincipal userPrincipal) {
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        if (chat.getGroup() == null && !chat.getMembers().contains(userInfo) || chat.getGroup() != null && !chat.getGroup().getMembers().contains(GroupMember.builder().member(userInfo).build())) {
            throw new AccessDeniedException("Bạn không phải là thành viên của phòng chat này");
        }

        Message message = Message.builder()
                .messageId(new ObjectId())
                .replyMessageId(messageRequestDTO.getReplyMessageId() != null ? new ObjectId(messageRequestDTO.getReplyMessageId()) : null)
                .content(messageRequestDTO.getContent())
                .attachments(messageRequestDTO.getAttachments())
                .status(MessageStatus.SENT)
                .type(MessageType.MESSAGE)
                .sender(userInfo)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        chat.getMessages().add(message);
        LastMessage lastMessage = LastMessage.builder()
                .createdAt(message.getCreatedAt())
                .sender(userInfo)
                .content(message.getContent() == null ? "[FILE]" : message.getContent())
                .build();
        chat.setLastMessage(lastMessage);
        chatRepository.save(chat);
        simpMessagingTemplate.convertAndSend("/chatroom/" + chatId, message);
        return message;
    }

    @Override
    public Message unsendMessage(String messageId, String chatId, UserPrincipal userPrincipal) {
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        if (chat.getGroup() == null && !chat.getMembers().contains(userInfo) || chat.getGroup() != null && !chat.getGroup().getMembers().contains(GroupMember.builder().member(userInfo).build())) {
            throw new AccessDeniedException("Bạn không phải là thành viên của phòng chat này");
        }

        int messageIndex = chat.getMessages().indexOf(Message.builder().messageId(new ObjectId(messageId)).build());
        if (messageIndex < 0) {
            throw new DataNotFoundException("Không tìm thấy tin nhắn");
        }
        Message message = chat.getMessages().get(messageIndex);
        if (!message.getSender().equals(userInfo)) {
            throw new AccessDeniedException("Chỉ người gửi mới được thu hồi");
        }
        if (ChronoUnit.HOURS.between(message.getCreatedAt(), LocalDateTime.now()) > 24) {
            throw new MessageRecallTimeExpiredException("Bạn chỉ có thể thu hồi tin nhắn trong 1 ngày sau khi gửi.");
        }
        message.setStatus(MessageStatus.UNSEND);
        chatRepository.save(chat);
        message.setContent("Tin nhắn đã bị thu hồi");
        message.setReactions(null);
        message.setAttachments(null);
        simpMessagingTemplate.convertAndSend("/chatroom/" + chatId, message);
        return message;
    }

    @Override
    public String deleteMessage(String messageId, String chatId, UserPrincipal userPrincipal) {
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        if (chat.getGroup() == null && !chat.getMembers().contains(userInfo) || chat.getGroup() != null && !chat.getGroup().getMembers().contains(GroupMember.builder().member(userInfo).build())) {
            throw new AccessDeniedException("Bạn không phải là thành viên của phòng chat này");
        }

        int messageIndex = chat.getMessages().indexOf(Message.builder().messageId(new ObjectId(messageId)).build());
        if (messageIndex < 0) {
            throw new DataNotFoundException("Không tìm thấy tin nhắn");
        }
        Message message = chat.getMessages().get(messageIndex);
        message.getDeleteBy().add(userInfo);
        chatRepository.save(chat);
        return "Xóa tin nhắn thành công";
    }

    @Override
    public void deleteMessage(MessageEventDTO messageEventDTO, String chatId) {
        UserInfo userInfo = userInfoRepository.findById(messageEventDTO.getSenderId()).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        if (chat.getGroup() == null && !chat.getMembers().contains(userInfo) || chat.getGroup() != null && !chat.getGroup().getMembers().contains(GroupMember.builder().member(userInfo).build())) {
            throw new AccessDeniedException("Bạn không phải là thành viên của phòng chat này");
        }

        int messageIndex = chat.getMessages().indexOf(Message.builder().messageId(new ObjectId(messageEventDTO.getMessageId())).build());
        if (messageIndex < 0) {
            throw new DataNotFoundException("Không tìm thấy tin nhắn");
        }
        Message message = chat.getMessages().get(messageIndex);
        message.getDeleteBy().add(userInfo);
        chatRepository.save(chat);
    }

    @Override
    public Message unsendMessage(MessageEventDTO messageEventDTO, String chatId) {
        UserInfo userInfo = userInfoRepository.findById(messageEventDTO.getSenderId()).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        if (chat.getGroup() == null && !chat.getMembers().contains(userInfo) || chat.getGroup() != null && !chat.getGroup().getMembers().contains(GroupMember.builder().member(userInfo).build())) {
            throw new AccessDeniedException("Bạn không phải là thành viên của phòng chat này");
        }

        int messageIndex = chat.getMessages().indexOf(Message.builder().messageId(new ObjectId(messageEventDTO.getMessageId())).build());
        if (messageIndex < 0) {
            throw new DataNotFoundException("Không tìm thấy tin nhắn");
        }
        Message message = chat.getMessages().get(messageIndex);
        if (!message.getSender().equals(userInfo)) {
            throw new AccessDeniedException("Chỉ người gửi mới được thu hồi");
        }
        if (ChronoUnit.HOURS.between(message.getCreatedAt(), LocalDateTime.now()) > 24) {
            throw new MessageRecallTimeExpiredException("Bạn chỉ có thể thu hồi tin nhắn trong 1 ngày sau khi gửi.");
        }
        message.setStatus(MessageStatus.UNSEND);
        chatRepository.save(chat);
        message.setContent("Tin nhắn đã bị thu hồi");
        message.setReactions(null);
        message.setAttachments(null);
        simpMessagingTemplate.convertAndSend("/chatroom/" + chatId, message);
        return message;
    }

}
