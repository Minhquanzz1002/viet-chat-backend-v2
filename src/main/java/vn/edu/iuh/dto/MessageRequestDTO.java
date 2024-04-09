package vn.edu.iuh.dto;

import lombok.*;
import vn.edu.iuh.models.Attachment;

import java.util.List;
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDTO {
    private String replyMessageId;
    private String content;
    private List<Attachment> attachments;
}
