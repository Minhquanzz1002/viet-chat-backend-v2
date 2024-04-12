package vn.edu.iuh.models;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.edu.iuh.models.enums.ReactionType;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"user", "type"})
@ToString
public class Reaction {
    @DocumentReference
    @Field("user_id")
    @JsonIncludeProperties({"id", "firstName", "lastName", "thumbnailAvatar", "gender"})
    private UserInfo user;
    private ReactionType type;
    private int quantity;
}
