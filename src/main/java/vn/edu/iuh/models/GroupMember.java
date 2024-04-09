package vn.edu.iuh.models;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.edu.iuh.models.enums.GroupMemberRole;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "member")
public class GroupMember {
    @Field("member_id")
    @DocumentReference(lazy = true)
    private UserInfo member;
    private GroupMemberRole role;
    private String joinMethod;
}
