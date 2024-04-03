package vn.edu.iuh.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserChat {
    @DocumentReference
    @Field("chat_id")
    private Chat chat;
    private LocalDateTime lastDeleteChatTime;
    private LocalDateTime joinTime;
}
