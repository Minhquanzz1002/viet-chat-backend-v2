package vn.edu.iuh.models;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.edu.iuh.models.enums.AttachmentType;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Attachment {
    private AttachmentType type;
    private String url;
    private String filename;
}
