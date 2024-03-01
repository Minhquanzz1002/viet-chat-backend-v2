package vn.edu.iuh.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    private String firstName;
    private String lastName;
    private String bio;
    private String thumbnailAvatar;
    private String coverImage;
    private boolean gender;
    private LocalDate birthday;
}
