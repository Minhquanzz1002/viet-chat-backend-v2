package vn.edu.iuh.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.edu.iuh.models.enums.FriendStatus;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friend {
    @DocumentReference(lazy = true, collection = "user_info")
    @Field("friend_id")
    private UserInfo profile;
    private String displayName;
    private FriendStatus status;
    @JsonIgnore
    @Field("chat_id")
    @DocumentReference(lazy = true, collection = "chats")
    private Chat chat;
    @JsonProperty("isBestFriend")
    @Field("is_best_friend")
    @Builder.Default
    private boolean isBestFriend = false;

    public Friend(UserInfo profile, String displayName, FriendStatus status) {
        this.profile = profile;
        this.displayName = displayName;
        this.status = status;
    }
}
