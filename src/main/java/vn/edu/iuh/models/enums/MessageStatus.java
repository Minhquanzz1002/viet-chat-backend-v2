package vn.edu.iuh.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageStatus {
    SENT("Gửi"),
    RECALLED("Thu hồi");

    private final String status;
}
