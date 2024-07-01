package vn.edu.iuh.dto;

import lombok.*;
import vn.edu.iuh.models.LastMessage;
import vn.edu.iuh.models.enums.UserChatStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChatRoomDTO {
    private String id;
    private LastMessage lastMessage;
    private String name;
    private String avatar;
    private String groupId;
    private UserChatStatus status;
    private String lastSeenMessageId;
}
