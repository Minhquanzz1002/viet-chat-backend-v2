package vn.edu.iuh.dto;

import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;

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
    private Boolean gender;
    @Past(message = "Ngày sinh phải trước ngày hiện tại")
    private LocalDate birthday;
}
