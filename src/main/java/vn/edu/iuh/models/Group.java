package vn.edu.iuh.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "groups")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Group {
    @Id
    private String id;
    private String name;
    private String thumbnailAvatar;
    @Builder.Default
    private List<GroupMember> members = new ArrayList<>();
    @CreatedDate
    private LocalDateTime createdAt;
}
