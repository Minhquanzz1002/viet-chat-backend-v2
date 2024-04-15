package vn.edu.iuh.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@JsonIgnoreProperties({"user"})
public class OtherUserInfoDTO {
    private String id;
    private String phone;
    private String firstName;
    private String lastName;
    private String bio;
    private String thumbnailAvatar;
    private String coverImage;
    private boolean gender;
    private LocalDate birthday;
    private LocalDateTime createdAt;
}
