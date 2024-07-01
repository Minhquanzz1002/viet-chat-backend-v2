package vn.edu.iuh.dto;


import lombok.*;
import vn.edu.iuh.models.enums.FriendStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class OtherUserInfoDTO {
    private String id;
    private String phone;
    private String displayName;
    private FriendStatus status;
    private String firstName;
    private String lastName;
    private String bio;
    private String thumbnailAvatar;
    private String coverImage;
    private boolean gender;
    private LocalDate birthday;
    private LocalDateTime createdAt;
}
