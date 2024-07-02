package vn.edu.iuh.dto;

import lombok.*;
import vn.edu.iuh.models.GroupMember;
import vn.edu.iuh.models.enums.FriendStatus;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberDTO extends GroupMember {
    private FriendStatus status;
    private String displayName;
}
