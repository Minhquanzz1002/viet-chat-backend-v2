package vn.edu.iuh.services;

import vn.edu.iuh.dto.MessageDTO;
import vn.edu.iuh.dto.MessageEventDTO;
import vn.edu.iuh.dto.MessageRequestDTO;
import vn.edu.iuh.models.Chat;
import vn.edu.iuh.models.Message;
import vn.edu.iuh.security.UserPrincipal;

import java.util.List;

public interface ChatService {
    List<Message> getAllMessages(String chatId, UserPrincipal userPrincipal);
    Chat findById(String id);
    Message saveMessage(MessageDTO messageDTO, String chatId);
    Message saveMessage(MessageRequestDTO messageRequestDTO, String chatId, UserPrincipal userPrincipal);
    Message unsendMessage(String messageId, String chatId, UserPrincipal userPrincipal);
    String deleteMessage(String messageId, String chatId, UserPrincipal userPrincipal);
    void deleteMessage(MessageEventDTO messageEventDTO, String chatId);
    Message unsendMessage(MessageEventDTO messageEventDTO, String chatId);
    String seenMessage(String chatId, UserPrincipal userPrincipal);
}
