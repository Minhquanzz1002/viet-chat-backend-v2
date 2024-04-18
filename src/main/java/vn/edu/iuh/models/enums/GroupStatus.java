package vn.edu.iuh.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GroupStatus {
    ACTIVE("Bình thường"), DELETED("Xóa");
    private final String description;
}
