package vn.edu.iuh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.validator.constraints.Length;
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    private String phone;
    @Length(min = 8, max = 32)
    private String password;
}
