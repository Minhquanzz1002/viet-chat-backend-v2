package vn.edu.iuh.dto;

import lombok.*;
import vn.edu.iuh.models.enums.GroupMemberRole;
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GroupRoleUpdateRequestDTO {
    private GroupMemberRole role;
}
