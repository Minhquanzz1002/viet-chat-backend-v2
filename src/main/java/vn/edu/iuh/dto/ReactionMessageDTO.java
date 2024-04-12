package vn.edu.iuh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import vn.edu.iuh.models.enums.ReactionType;
@AllArgsConstructor
@Data
public class ReactionMessageDTO {
    private ReactionType type;
    private int quantity;
}
