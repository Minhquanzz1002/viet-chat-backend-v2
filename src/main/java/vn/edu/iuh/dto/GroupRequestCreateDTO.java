package vn.edu.iuh.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GroupRequestCreateDTO {
    @NotNull(message = "Tên nhóm là bắt buộc")
    @NotEmpty(message = "Tên nhóm không được rỗng")
    private String name;
    private String thumbnailAvatar;
    @NotNull(message = "Thành viên nhóm là bắt buộc")
    @Size(min = 2, message = "Mời tối thiểu 2 người khác để tạo nhóm")
    private List<String> members;
}
