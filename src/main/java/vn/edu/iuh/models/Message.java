package vn.edu.iuh.models;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Message {
    private ObjectId messageId;
    @DocumentReference
    private UserInfo senderId;
    private String content;
    private List<Attachment> attachments;
    @CreatedDate
    private LocalDateTime createdAt;
    public Message(List<Attachment> attachments) {
        this.messageId = new ObjectId();
        this.attachments = attachments;
    }

    public Message(String content, List<Attachment> attachments) {
        this.messageId = new ObjectId();
        this.content = content;
        this.attachments = attachments;
    }

    public Message(String content) {
        this.messageId = new ObjectId();
        this.content = content;
    }
}
