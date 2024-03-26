package vn.edu.iuh.services;

import vn.edu.iuh.dto.MessageRequestDTO;
import vn.edu.iuh.models.Chat;
import vn.edu.iuh.models.Message;
import vn.edu.iuh.security.UserPrincipal;

import java.util.List;

public interface ChatService {
    List<Message> getAllMessages(String chatId);
    Chat findById(String id);

    Message sendMessage(MessageRequestDTO messageRequestDTO, String chatId, UserPrincipal sender);
}
