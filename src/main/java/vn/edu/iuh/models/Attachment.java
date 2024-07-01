package vn.edu.iuh.models;

import lombok.*;
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
    private int size;
}
