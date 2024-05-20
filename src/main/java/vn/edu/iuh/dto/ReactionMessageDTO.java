package vn.edu.iuh.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import vn.edu.iuh.models.enums.ReactionType;
@AllArgsConstructor
@Data
public class ReactionMessageDTO {
    @NotNull(message = "Loại cảm xúc là bắt buộc")
    private ReactionType type;
    @Min(value = 1, message = "Số lượng cảm xúc tối thiểu là 1")
    private int quantity;
}
