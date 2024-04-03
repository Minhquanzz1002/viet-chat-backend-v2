package vn.edu.iuh.models;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LastMessage {
    private String content;
    @Field("sender_id")
    @DocumentReference
    @JsonIncludeProperties({"id", "firstName", "lastName", "thumbnailAvatar", "gender"})
    private UserInfo sender;
    private LocalDateTime createdAt;
}
