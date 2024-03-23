package vn.edu.iuh.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "chats")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Chat {
    @Id
    private String id;
    @Field("is_group")
    @JsonProperty("isGroup")
    private boolean isGroup;
    private List<Message> messages;
    @DocumentReference
    private List<UserInfo> members;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Chat(boolean isGroup, List<Message> messages) {
        this.isGroup = isGroup;
        this.messages = messages;
    }

    public Chat(String id) {
        this.id = id;
    }
}
