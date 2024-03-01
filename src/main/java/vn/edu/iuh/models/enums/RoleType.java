package vn.edu.iuh.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleType {
    ADMIN("Quản trị viên"),
    USER("Người dùng cơ bản");

    private final String description;
}
