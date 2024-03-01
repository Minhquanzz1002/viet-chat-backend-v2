package vn.edu.iuh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GroupRequestCreateDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String thumbnailAvatar;
    @NotNull
    private List<String> members;
}
