package vn.edu.iuh.dto;

import lombok.*;
import vn.edu.iuh.models.Attachment;
import vn.edu.iuh.models.enums.MessageStatus;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class MessageDTO {
    private String sender;
    private String replyMessageId;
    private String content;
    private List<Attachment> attachments;
}
