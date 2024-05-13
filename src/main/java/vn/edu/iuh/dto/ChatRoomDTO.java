package vn.edu.iuh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import vn.edu.iuh.models.LastMessage;

import java.time.LocalDateTime;

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
    private boolean hidden;
    private LocalDateTime pinnedAt;
    private String lastSeenMessageId;
}
