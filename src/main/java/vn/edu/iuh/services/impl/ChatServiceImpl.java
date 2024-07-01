package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.*;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.exceptions.InvalidRequestException;
import vn.edu.iuh.exceptions.MessageRecallTimeExpiredException;
import vn.edu.iuh.models.*;
import vn.edu.iuh.models.enums.MessageStatus;
import vn.edu.iuh.models.enums.MessageType;
import vn.edu.iuh.models.enums.UserChatStatus;
import vn.edu.iuh.repositories.ChatRepository;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.ChatService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    private final ModelMapper modelMapper;
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
    public Page<Message> getAllMessages(String chatId, UserPrincipal userPrincipal, Pageable pageable, String content) {
        UserInfo senderInfo = findUserInfoByUserPrincipal(userPrincipal);
        Chat chat = findById(chatId);

        UserChat userChat = senderInfo.getChats().stream()
                .filter(userChat1 -> userChat1.getChat().getId().equals(chatId))
                .findFirst()
                .get();

        checkChatMembership(chat, senderInfo);

        List<Message> messages = chat.getMessages().stream()
                .filter(message -> userChat.getLastDeleteChatTime() == null || message.getCreatedAt().isAfter(userChat.getLastDeleteChatTime()))
                .filter(message -> !message.getDeleteBy().contains(senderInfo))
                .filter(message -> content == null || message.getContent().toLowerCase().contains(content.toLowerCase()))
                .peek(message -> {
                    if (message.getStatus().equals(MessageStatus.UNSEND)) {
                        message.setContent("Tin nhắn đã bị thu hồi");
                        message.setAttachments(null);
                        message.setReactions(null);
                    }
                })
                .toList();

        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int start = Math.max(0, messages.size() - pageSize - (pageNumber * pageSize));
        int end = messages.size() - (pageNumber * pageSize);
        List<Message> responseMessage = messages.subList(start, end);
        return new PageImpl<>(responseMessage, pageable, messages.size());
    }

    @Override
    public Chat findById(String id) {
        return chatRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + id));
    }

    @Override
    public ChatDTO findChatById(String id, UserPrincipal userPrincipal) {
        UserInfo senderInfo = findUserInfoByUserPrincipal(userPrincipal);
        Chat chat = findById(id);
        ChatDTO chatDTO = modelMapper.map(chat, ChatDTO.class);

        Group group = chat.getGroup();
        boolean isGroup = (group != null);
        String name = isGroup ? chat.getGroup().getName() : UserInfoServiceImpl.getMemberName(chat, senderInfo);
        String avatar = isGroup ? chat.getGroup().getThumbnailAvatar() : UserInfoServiceImpl.getMemberAvatar(chat, senderInfo);
        chatDTO.setName(name);
        chatDTO.setAvatar(avatar);
        return chatDTO;
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
        List<Message> messages = new ArrayList<>(chat.getMessages());
        messages.add(message);
        chat.setMessages(messages);
        LastMessage lastMessage = buildLastMessage(message, sender);

        chat.setLastMessage(lastMessage);
        chatRepository.save(chat);
        int index = sender.getChats().indexOf(UserChat.builder().chat(chat).build());
        UserChat userChat = sender.getChats().get(index);
        userChat.setLastSeenMessageId(message.getMessageId());
        userInfoRepository.save(sender);
        return message;
    }

    @Override
    public Message saveMessage(MessageRequestDTO messageRequestDTO, String chatId, UserPrincipal userPrincipal) {
        UserInfo senderInfo = findUserInfoByUserPrincipal(userPrincipal);
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
        checkChatMembership(chat, senderInfo);
        Message message = Message.builder()
                .messageId(new ObjectId())
                .replyMessageId(messageRequestDTO.getReplyMessageId() != null ? new ObjectId(messageRequestDTO.getReplyMessageId()) : null)
                .content(messageRequestDTO.getContent())
                .attachments(messageRequestDTO.getAttachments())
                .status(MessageStatus.SENT)
                .type(MessageType.MESSAGE)
                .sender(senderInfo)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        List<Message> messages = new ArrayList<>(chat.getMessages());
        messages.add(message);
        chat.setMessages(messages);
        LastMessage lastMessage = buildLastMessage(message, senderInfo);
        chat.setLastMessage(lastMessage);

        //
        chat.getDeleteBy().forEach((userInfoIdDeleted) -> {
            UserInfo userInfoDeleted = userInfoRepository.findById(userInfoIdDeleted).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
            userInfoDeleted.getChats().stream()
                    .filter(userChat -> userChat.getChat().getId().equals(chatId))
                    .findFirst()
                    .map(userChat -> {
                        userChat.setStatus(UserChatStatus.NORMAL);
                        return userChat;
                    });
            userInfoRepository.save(userInfoDeleted);
        });
        chat.setDeleteBy(new ArrayList<>());

        chatRepository.save(chat);
        simpMessagingTemplate.convertAndSend("/chatroom/" + chatId, message);
        return message;
    }

    @Override
    public Message unsendMessage(String messageId, String chatId, UserPrincipal userPrincipal) {
        UserInfo sender = findUserInfoByUserPrincipal(userPrincipal);
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
    public String deleteAllMessages(UserPrincipal userPrincipal, String chatId) {
        UserInfo senderInfo = findUserInfoByUserPrincipal(userPrincipal);
        Optional<UserChat> chatToUpdate = senderInfo.getChats().stream()
                .filter(userChat -> userChat.getChat().getId().equals(chatId))
                .findFirst();
        if (chatToUpdate.isPresent()) {
            UserChat userChat = chatToUpdate.get();
            userChat.setLastDeleteChatTime(LocalDateTime.now());
            userInfoRepository.save(senderInfo);
            return "Xóa lịch sử trò chuyện thành công";
        }
        throw new DataNotFoundException("Không tìm thấy phòng chat");
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
        UserInfo sender = findUserInfoByUserPrincipal(userPrincipal);
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
    public String seenMessage(String chatId, UserPrincipal userPrincipal) {
        UserInfo userInfo = findUserInfoByUserPrincipal(userPrincipal);
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
        Chat chat = findChatById(chatId);
        checkChatMembership(chat, userInfo);
        int index = userInfo.getChats().indexOf(UserChat.builder().chat(chat).build());
        userInfo.getChats().get(index).setLastSeenMessageId(chat.getMessages().get(chat.getMessages().size() - 1).getMessageId());
        userInfoRepository.save(userInfo);
    }

    @Override
    public Message pinMessage(UserPrincipal userPrincipal, String chatId, String messageId) {
        UserInfo senderInfo = findUserInfoByUserPrincipal(userPrincipal);
        Chat chatroom = findChatById(chatId);
        checkChatMembership(chatroom, senderInfo);

        Message messageToPin = chatroom.getMessages().stream()
                .filter(msg -> msg.getMessageId().equals(messageId))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy tin nhắn"));

        if (chatroom.getPinnedMessages().contains(messageToPin)) {
            throw new InvalidRequestException("Tin nhắn đã được ghim trước đó");
        }
        if (chatroom.getPinnedMessages().size() >= 3) {
            throw new InvalidRequestException("Số lượng tin nhắn ghim tối đa là 3");
        }

        chatroom.getPinnedMessages().add(messageToPin);

        Message messageEvent = Message.builder()
                .messageId(new ObjectId())
                .type(MessageType.EVENT)
                .status(MessageStatus.SENT)
                .content("{" + senderInfo.getId() + "}" + " đã ghim một tin nhắn")
                .createdAt(LocalDateTime.now())
                .build();
        chatroom.getMessages().add(messageEvent);

        chatroom.getDeleteBy().forEach((userInfoIdDeleted) -> {
            UserInfo userInfoDeleted = userInfoRepository.findById(userInfoIdDeleted).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
            userInfoDeleted.getChats().stream()
                    .filter(userChat -> userChat.getChat().getId().equals(chatId))
                    .findFirst()
                    .map(userChat -> {
                        userChat.setStatus(UserChatStatus.NORMAL);
                        return userChat;
                    });
            userInfoRepository.save(userInfoDeleted);
        });
        chatroom.setDeleteBy(new ArrayList<>());


        LastMessage lastMessage = buildLastMessage(messageEvent, senderInfo);
        chatroom.setLastMessage(lastMessage);

        chatRepository.save(chatroom);

        simpMessagingTemplate.convertAndSend("/chatroom/" + chatId, messageEvent);
        return messageEvent;
    }

    @Override
    public Message unpinMessage(UserPrincipal userPrincipal, String chatId, String messageId) {
        UserInfo senderInfo = findUserInfoByUserPrincipal(userPrincipal);
        Chat chatroom = findChatById(chatId);
        checkChatMembership(chatroom, senderInfo);

        Message messageToUnpin = chatroom.getMessages().stream()
                .filter(msg -> msg.getMessageId().equals(messageId))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy tin nhắn"));

        if (!chatroom.getPinnedMessages().contains(messageToUnpin)) {
            throw new InvalidRequestException("Tin nhắn không được ghim trước đó");
        }

        chatroom.getPinnedMessages().remove(messageToUnpin);

        Message messageEvent = Message.builder()
                .messageId(new ObjectId())
                .type(MessageType.EVENT)
                .status(MessageStatus.SENT)
                .content("{" + senderInfo.getId() + "}" + " đã bỏ ghim một tin nhắn")
                .createdAt(LocalDateTime.now())
                .build();
        chatroom.getMessages().add(messageEvent);


        chatroom.getDeleteBy().forEach((userInfoIdDeleted) -> {
            UserInfo userInfoDeleted = userInfoRepository.findById(userInfoIdDeleted).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
            userInfoDeleted.getChats().stream()
                    .filter(userChat -> userChat.getChat().getId().equals(chatId))
                    .findFirst()
                    .map(userChat -> {
                        userChat.setStatus(UserChatStatus.NORMAL);
                        return userChat;
                    });
            userInfoRepository.save(userInfoDeleted);
        });
        chatroom.setDeleteBy(new ArrayList<>());

        LastMessage lastMessage = buildLastMessage(messageEvent, senderInfo);
        chatroom.setLastMessage(lastMessage);

        chatRepository.save(chatroom);

        simpMessagingTemplate.convertAndSend("/chatroom/" + chatId, messageEvent);
        return messageEvent;
    }

    private LastMessage buildLastMessage(Message message, UserInfo senderInfo) {
        return LastMessage.builder()
                .messageId(message.getMessageId())
                .createdAt(message.getCreatedAt())
                .sender(senderInfo)
                .content(message.getContent() == null ? "[FILE]" : message.getContent())
                .build();
    }

    private UserInfo findUserInfoByUserPrincipal(UserPrincipal userPrincipal) {
        return userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
    }

    private Chat findChatById(String chatId) {
        return chatRepository.findById(chatId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat có ID " + chatId));
    }
}
