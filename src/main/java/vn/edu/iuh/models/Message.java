package vn.edu.iuh.models;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.edu.iuh.models.enums.MessageStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Message {
    private ObjectId messageId;
    private ObjectId replyMessageId;
    @DocumentReference
    @Field("sender_id")
    @JsonIncludeProperties({"id", "firstName", "lastName", "thumbnailAvatar", "gender"})
    private UserInfo sender;
    private String content;
    private List<Attachment> attachments;
    private List<Reaction> reactions;
    private MessageStatus status = MessageStatus.SENT;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @DocumentReference
    private List<UserInfo> deleteBy = new ArrayList<>();

    public Message(ObjectId replyMessageId, UserInfo sender, String content, List<Attachment> attachments, List<Reaction> reactions, MessageStatus status) {
        this.messageId = new ObjectId();
        this.replyMessageId = replyMessageId;
        this.sender = sender;
        this.content = content;
        this.attachments = attachments;
        this.reactions = reactions;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Message(String replyMessageId, UserInfo sender, String content, List<Attachment> attachments, List<Reaction> reactions, MessageStatus status) {
        this.messageId = new ObjectId();
        this.replyMessageId = new ObjectId(replyMessageId);
        this.sender = sender;
        this.content = content;
        this.attachments = attachments;
        this.reactions = reactions;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Message(UserInfo sender, String content, List<Attachment> attachments, List<Reaction> reactions, MessageStatus status) {
        this.messageId = new ObjectId();
        this.sender = sender;
        this.content = content;
        this.attachments = attachments;
        this.reactions = reactions;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getMessageId() {
        return messageId.toString();
    }

    public String getReplyMessageId() {
        return replyMessageId == null ? null : replyMessageId.toString();
    }
}
