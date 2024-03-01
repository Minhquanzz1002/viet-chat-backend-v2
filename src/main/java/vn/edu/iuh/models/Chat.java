package vn.edu.iuh.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Document(collection = "chats")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Chat {
    @Id
    private String id;
    private List<Message> messages;
    @CreatedDate
    private LocalDateTime createdAt;
}
