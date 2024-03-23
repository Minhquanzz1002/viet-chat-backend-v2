package vn.edu.iuh.models.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    UNVERIFIED("Số điện thoại chưa xác thực"),
    ACTIVE("Đang hoạt động"),
    LOCKED("Tài khoản bị khóa");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

}
