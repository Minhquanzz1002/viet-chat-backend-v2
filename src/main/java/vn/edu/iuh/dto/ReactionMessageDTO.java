package vn.edu.iuh.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import vn.edu.iuh.models.enums.ReactionType;
@AllArgsConstructor
@Data
public class ReactionMessageDTO {
    private ReactionType type;
    @Min(value = 1, message = "Số lượng cảm xúc tối thiểu là 1")
    private int quantity;
}
