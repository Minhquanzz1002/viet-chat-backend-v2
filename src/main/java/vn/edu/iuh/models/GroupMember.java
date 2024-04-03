package vn.edu.iuh.models;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.edu.iuh.models.enums.GroupMemberRole;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GroupMember {
    @Field("member_id")
    @DocumentReference(lazy = true)
    private UserInfo user;
    private GroupMemberRole role;
    private String joinMethod;
}
