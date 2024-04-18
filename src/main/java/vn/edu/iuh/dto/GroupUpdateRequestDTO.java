package vn.edu.iuh.dto;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GroupUpdateRequestDTO {
    private String name;
    private String thumbnailAvatar;
}
