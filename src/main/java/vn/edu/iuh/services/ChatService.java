package vn.edu.iuh.services;

import vn.edu.iuh.dto.MessageDTO;
import vn.edu.iuh.models.Chat;
import vn.edu.iuh.models.Message;

import java.util.List;

public interface ChatService {
    List<Message> getAllMessages(String chatId);
    Chat findById(String id);
    Message saveMessage(MessageDTO messageDTO, String chatId);
}
