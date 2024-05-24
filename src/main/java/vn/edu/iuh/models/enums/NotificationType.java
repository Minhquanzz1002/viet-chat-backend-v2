package vn.edu.iuh.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotificationType {
    NEW_MESSAGE("Tin nhắn mới"),
    FRIEND_REQUEST("Đối phương gửi lời mời kết bạn cho bạn"),
    ACCEPT_FRIEND_REQUEST("Đối phương chấp nhận lời mời kết bạn"),
    DECLINE_FRIEND_REQUEST("Bị từ chối kết bạn"),
    CANCEL_FRIEND_REQUEST("Đối phương thu hồi lời mời"),
    BLOCKED_FRIEND("Bị đối phương block"),
    UNBLOCKED_FRIEND("Được unblock"),
    DELETED_FRIEND("Đối phương xóa kết bạn"),
    ADD_TO_GROUP("Được thêm vào nhóm"),
    REMOVED_FROM_GROUP("Bị xóa khỏi nhóm"),
    DELETED_GROUP("Nhóm bị giải tán"),
    CREATED_GROUP("Nhóm mới tạo");
    private final String description;
}
