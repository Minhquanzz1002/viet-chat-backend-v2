package vn.edu.iuh.dto;

import lombok.*;
import vn.edu.iuh.models.GroupMember;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {
    private String id;
    private String name;
    private String thumbnailAvatar;
    private List<GroupMember> members;
    private String chatId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
