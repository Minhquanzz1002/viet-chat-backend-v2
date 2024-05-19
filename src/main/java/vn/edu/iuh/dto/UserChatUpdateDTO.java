package vn.edu.iuh.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.edu.iuh.models.enums.UserChatStatus;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserChatUpdateDTO {
    @NotNull(message = "Trạng thái là bắt buộc")
    private UserChatStatus status;
}
