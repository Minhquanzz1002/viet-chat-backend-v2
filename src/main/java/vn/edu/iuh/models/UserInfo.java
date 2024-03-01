package vn.edu.iuh.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.*;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "user_info")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@JsonIgnoreProperties({"user"})
public class UserInfo {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String bio;
    private String thumbnailAvatar;
    private String coverImage;
    private boolean gender;
    private LocalDate birthday;
    @DocumentReference
    @JsonIgnore
    @ToString.Exclude
    private List<Group> groups = new ArrayList<>();
    @JsonIgnore
    @ToString.Exclude
    private List<Friend> friends = new ArrayList<>();
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @Field("user_id")
    @Indexed(unique = true)
    @DocumentReference
    private User user;

    public UserInfo(String firstName, String lastName, String bio, String thumbnailAvatar, String coverImage, boolean gender, LocalDate birthday, User user) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
        this.thumbnailAvatar = thumbnailAvatar;
        this.coverImage = coverImage;
        this.gender = gender;
        this.birthday = birthday;
        this.user = user;
    }
}
