package vn.edu.iuh.models;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.edu.iuh.models.enums.FriendStatus;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Friend {
    @DocumentReference
    @Field("friend_id")
    private UserInfo user;
    private String displayName;
    private FriendStatus status;
}
