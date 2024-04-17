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
import vn.edu.iuh.dto.ReactionMessageDTO;
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

    private void checkChatMembership(Chat chat, UserInfo sender) {
        if ((chat.getGroup() == null && !chat.getMembers().contains(sender)) ||
                (chat.getGroup() != null && !chat.getGroup().getMembers().contains(GroupMember.builder().profile(sender).build()))) {
            throw new AccessDeniedException("Bạn không phải là thành viên của phòng chat này");
        }
    }

    @Override
    public List<Message> getAllMessages(String chatId, UserPrincipal userPrincipal) {
        UserInfo sender = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = findById(chatId);

        checkChatMembership(chat, sender);

        List<Message> messages = new ArrayList<>();
        chat.getMessages().forEach(
                message -> {
                    if (!message.getDeleteBy().contains(sender)) {
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
        if (chat.getMessages() == null) {
            chat.setMessages(new ArrayList<>());
        }
        chat.getMessages().add(message);
        LastMessage lastMessage = LastMessage.builder()
                .messageId(message.getMessageId())
                .createdAt(message.getCreatedAt())
                .sender(sender)
                .content(message.getContent() == null ? "[FILE]" : message.getContent())
                .build();
        chat.setLastMessage(lastMessage);
        chatRepository.save(chat);
        int index = sender.getChats().indexOf(UserChat.builder().chat(chat).build());
        log.info("day la vi tri {}", index);
        UserChat userChat = sender.getChats().get(index);
        userChat.setLastSeenMessageId(message.getMessageId());
        userInfoRepository.save(sender);
        return message;
    }

    @Override
    public Message saveMessage(MessageRequestDTO messageRequestDTO, String chatId, UserPrincipal userPrincipal) {
        UserInfo sender = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        checkChatMembership(chat, sender);
        Message message = Message.builder()
                .messageId(new ObjectId())
                .replyMessageId(messageRequestDTO.getReplyMessageId() != null ? new ObjectId(messageRequestDTO.getReplyMessageId()) : null)
                .content(messageRequestDTO.getContent())
                .attachments(messageRequestDTO.getAttachments())
                .status(MessageStatus.SENT)
                .type(MessageType.MESSAGE)
                .sender(sender)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        List<Message> messages = new ArrayList<>(chat.getMessages());
        messages.add(message);
        chat.setMessages(messages);
        LastMessage lastMessage = LastMessage.builder()
                .messageId(message.getMessageId())
                .createdAt(message.getCreatedAt())
                .sender(sender)
                .content(message.getContent() == null ? "[FILE]" : message.getContent())
                .build();
        chat.setLastMessage(lastMessage);
        chatRepository.save(chat);
        simpMessagingTemplate.convertAndSend("/chatroom/" + chatId, message);
        return message;
    }

    @Override
    public Message unsendMessage(String messageId, String chatId, UserPrincipal userPrincipal) {
        UserInfo sender = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));

        checkChatMembership(chat, sender);

        int messageIndex = chat.getMessages().indexOf(Message.builder().messageId(new ObjectId(messageId)).build());
        if (messageIndex < 0) {
            throw new DataNotFoundException("Không tìm thấy tin nhắn");
        }
        Message message = chat.getMessages().get(messageIndex);
        if (!message.getSender().equals(sender)) {
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
        UserInfo sender = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        checkChatMembership(chat, sender);

        int messageIndex = chat.getMessages().indexOf(Message.builder().messageId(new ObjectId(messageId)).build());
        if (messageIndex < 0) {
            throw new DataNotFoundException("Không tìm thấy tin nhắn");
        }
        Message message = chat.getMessages().get(messageIndex);
        message.getDeleteBy().add(sender);
        chatRepository.save(chat);
        return "Xóa tin nhắn thành công";
    }

    @Override
    public void deleteMessage(MessageEventDTO messageEventDTO, String chatId) {
        UserInfo sender = userInfoRepository.findById(messageEventDTO.getSenderId()).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        checkChatMembership(chat, sender);

        int messageIndex = chat.getMessages().indexOf(Message.builder().messageId(new ObjectId(messageEventDTO.getMessageId())).build());
        if (messageIndex < 0) {
            throw new DataNotFoundException("Không tìm thấy tin nhắn");
        }
        Message message = chat.getMessages().get(messageIndex);
        message.getDeleteBy().add(sender);
        chatRepository.save(chat);
    }

    @Override
    public Message unsendMessage(MessageEventDTO messageEventDTO, String chatId) {
        UserInfo userInfo = userInfoRepository.findById(messageEventDTO.getSenderId()).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        checkChatMembership(chat, userInfo);

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

    @Override
    public Message reactionMessage(String messageId, String chatId, UserPrincipal userPrincipal, ReactionMessageDTO reactionMessageDTO) {
        Chat chat = findById(chatId);
        UserInfo sender = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        checkChatMembership(chat, sender);
        int messageIndex = chat.getMessages().indexOf(Message.builder().messageId(new ObjectId(messageId)).build());
        if (messageIndex < 0) {
            throw new DataNotFoundException("Không tìm thấy tin nhắn");
        }
        Message message = chat.getMessages().get(messageIndex);
        List<Reaction> reactions = message.getReactions();
        Reaction reaction = new Reaction(sender, reactionMessageDTO.getType(), reactionMessageDTO.getQuantity());
        if (reactions.contains(reaction)) {
            int index = reactions.indexOf(reaction);
            Reaction existingReaction = reactions.remove(index);
            existingReaction.setQuantity(existingReaction.getQuantity() + reaction.getQuantity());
            reactions.add(0, existingReaction);
//            reactions.get(index).setQuantity(reactions.get(index).getQuantity() + reaction.getQuantity());
        } else {
            reactions.add(0, reaction);
        }
        chatRepository.save(chat);
        simpMessagingTemplate.convertAndSend("/chatroom/" + chatId, message);
        return message;
    }

    @Override
    public Message deleteReactionsMessage(String messageId, String chatId, UserPrincipal userPrincipal) {
        Chat chat = findById(chatId);
        UserInfo sender = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        checkChatMembership(chat, sender);
        int messageIndex = chat.getMessages().indexOf(Message.builder().messageId(new ObjectId(messageId)).build());
        if (messageIndex < 0) {
            throw new DataNotFoundException("Không tìm thấy tin nhắn");
        }
        Message message = chat.getMessages().get(messageIndex);
        List<Reaction> reactions = message.getReactions();
        reactions.removeIf(reaction -> reaction.getUser().equals(sender));
        chatRepository.save(chat);
        simpMessagingTemplate.convertAndSend("/chatroom/" + chatId, message);
        return message;
    }

    @Override
    public String seenMessage(String chatId,  UserPrincipal userPrincipal) {
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        checkChatMembership(chat, userInfo);
        int index = userInfo.getChats().indexOf(UserChat.builder().chat(chat).build());
        userInfo.getChats().get(index).setLastSeenMessageId(chat.getMessages().get(chat.getMessages().size() - 1).getMessageId());
        userInfoRepository.save(userInfo);
        return "Thành công";
    }

    @Override
    public void seenMessage(String chatId, String userInfoId) {
        UserInfo userInfo = userInfoRepository.findById(userInfoId).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        checkChatMembership(chat, userInfo);
        int index = userInfo.getChats().indexOf(UserChat.builder().chat(chat).build());
        userInfo.getChats().get(index).setLastSeenMessageId(chat.getMessages().get(chat.getMessages().size() - 1).getMessageId());
        userInfoRepository.save(userInfo);
    }

    @Override
    public List<Message> findByChat(String id,String mes) {
        Chat chat = chatRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + id));
        List<Message> list =  chat.getMessages();
        List<Message> listFine =new ArrayList<>();
        for (Message message : list) {
            if (message.getContent().contains(mes)) {
                listFine.add(message);
            }
        }
        return listFine;
    }

}
