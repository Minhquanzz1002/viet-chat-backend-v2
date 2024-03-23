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
    private String sender;
    private String receiver;
    private String date;
    public enum Status {
        MESSAGE, JOIN, LEAVE
    }
}
