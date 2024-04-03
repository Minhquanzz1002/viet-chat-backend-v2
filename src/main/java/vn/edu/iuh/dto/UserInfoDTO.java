package vn.edu.iuh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
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
    private Boolean gender;
    @Past(message = "Ngày sinh phải trước ngày hiện tại")
    private LocalDate birthday;
}
