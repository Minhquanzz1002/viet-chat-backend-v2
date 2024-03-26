package vn.edu.iuh.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UploadType {
    AVATAR("Avatar Upload", "avatars/"),
    COVER_IMAGE("Cover Image Upload", "cover/"),
    MESSAGE("Message Attachment Upload", "messages/");
    private final String description;
    private final String link;
}
