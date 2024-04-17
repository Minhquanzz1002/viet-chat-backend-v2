package vn.edu.iuh.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {
    private String id;
    private String name;
    private String thumbnailAvatar;
    private String chatId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
