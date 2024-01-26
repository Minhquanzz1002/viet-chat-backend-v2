package vn.edu.iuh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    @Length(min = 10, max = 10)
    private String phone;
    @Length(min = 8, max = 32)
    private String password;
}
