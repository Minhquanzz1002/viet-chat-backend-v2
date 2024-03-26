package vn.edu.iuh.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.iuh.models.enums.NotificationType;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Notification {
    private String message;
    private NotificationType type;
    private String sender;
    private LocalDateTime timestamp;
}
