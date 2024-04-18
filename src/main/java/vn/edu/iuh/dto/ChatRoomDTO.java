package vn.edu.iuh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import vn.edu.iuh.models.LastMessage;

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
    @JsonProperty("isGroup")
    private boolean isGroup;
    private String groupId;
    private String lastSeenMessageId;
}
