package vn.edu.iuh.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.edu.iuh.dto.MessageDTO;
import vn.edu.iuh.dto.MessageEventDTO;
import vn.edu.iuh.dto.MessageRequestDTO;
import vn.edu.iuh.dto.ReactionMessageDTO;
import vn.edu.iuh.models.Chat;
import vn.edu.iuh.models.Message;
import vn.edu.iuh.security.UserPrincipal;

public interface ChatService {
    Page<Message> getAllMessages(String chatId, UserPrincipal userPrincipal, Pageable pageable, String content);

    Chat findById(String id);

    Message saveMessage(MessageDTO messageDTO, String chatId);

    Message saveMessage(MessageRequestDTO messageRequestDTO, String chatId, UserPrincipal userPrincipal);

    Message unsendMessage(String messageId, String chatId, UserPrincipal userPrincipal);

    String deleteMessage(String messageId, String chatId, UserPrincipal userPrincipal);

    void deleteMessage(MessageEventDTO messageEventDTO, String chatId);
    String deleteAllMessages(UserPrincipal userPrincipal, String chatId);
    Message unsendMessage(MessageEventDTO messageEventDTO, String chatId);

    Message reactionMessage(String messageId, String chatId, UserPrincipal userPrincipal, ReactionMessageDTO reactionMessageDTO);
    Message deleteReactionsMessage(String messageId, String chatId, UserPrincipal userPrincipal);

    String seenMessage(String chatId, UserPrincipal userPrincipal);

    void seenMessage(String chatId, String userInfoId);

    Message pinMessage(UserPrincipal userPrincipal, String chatId, String messageId);
    Message unpinMessage(UserPrincipal userPrincipal, String chatId, String messageId);
}
