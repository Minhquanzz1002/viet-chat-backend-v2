package vn.edu.iuh.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FriendStatus {
    FRIEND("Bạn bè"),
    FRIEND_REQUEST("Mời kết bạn"),
    PENDING("Chờ chấp nhận"),
    BLOCK("Chặn"),
    BLOCKED("Bị chặn"),
    STRANGER("Người lạ");
    private final String description;
}
