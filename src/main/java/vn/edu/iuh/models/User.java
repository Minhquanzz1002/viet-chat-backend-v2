package vn.edu.iuh.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.edu.iuh.models.enums.RoleType;
import vn.edu.iuh.models.enums.UserStatus;

import java.time.LocalDateTime;

@Document("users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    @Length(min = 10, max = 10)
    private String phone;
    @Length(min = 8, max = 32)
    private String password;
    private RoleType role;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    private UserStatus status;

    public User(String phone, String password, UserStatus status, RoleType role) {
        this.phone = phone;
        this.password = password;
        this.status = status;
        this.role = role;
    }

    public User(String id) {
        this.id = id;
    }
}
