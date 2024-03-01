package vn.edu.iuh.models;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private Status status;
    private String message;
    private String senderName;
    private String receiverName;
    private String date;
    public enum Status {
        MESSAGE, JOIN, LEAVE
    }
}
