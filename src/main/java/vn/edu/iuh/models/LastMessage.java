package vn.edu.iuh.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LastMessage {
    private String content;
    @DocumentReference
    private User user;
    @CreatedDate
    private LocalDateTime createdAt;
}
