package vn.edu.iuh.models;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "chat")
public class UserChat {
    @DocumentReference
    @Field("chat_id")
    private Chat chat;
    private LocalDateTime pinnedAt;
    @Builder.Default
    private boolean hidden = false;
    private String lastSeenMessageId;
    private LocalDateTime lastDeleteChatTime;
    private LocalDateTime joinTime;
}
