package vn.edu.iuh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.models.enums.FriendStatus;
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendDTO {
    private UserInfo profile;
    private String displayName;
    private FriendStatus status;
    private String chatId;
    @JsonProperty("isBestFriend")
    @Builder.Default
    private boolean isBestFriend = false;

}
