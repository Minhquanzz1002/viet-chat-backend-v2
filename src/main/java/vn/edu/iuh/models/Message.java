package vn.edu.iuh.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.edu.iuh.models.enums.MessageStatus;
import vn.edu.iuh.models.enums.MessageType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "messageId")
@ToString
@Builder
public class Message {
    private ObjectId messageId;
    private ObjectId replyMessageId;
    private MessageType type = MessageType.MESSAGE;
    @DocumentReference
    @Field("sender_id")
    @JsonIncludeProperties({"id", "firstName", "lastName", "thumbnailAvatar", "gender"})
    private UserInfo sender;
    private String content;
    private List<Attachment> attachments;
    private List<Reaction> reactions;
    private MessageStatus status = MessageStatus.SENT;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt = LocalDateTime.now();
    @JsonIgnore
    @DocumentReference(lazy = true)
    private List<UserInfo> deleteBy = new ArrayList<>();

    public String getMessageId() {
        return messageId.toString();
    }

    public String getReplyMessageId() {
        return replyMessageId == null ? null : replyMessageId.toString();
    }
}
