package vn.edu.iuh.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GroupMemberRole {
    GROUP_LEADER("Trưởng nhóm"),
    DEPUTY_GROUP_LEADER("Phó nhóm"),
    MEMBER("Thành viên");

    private final String description;
}
