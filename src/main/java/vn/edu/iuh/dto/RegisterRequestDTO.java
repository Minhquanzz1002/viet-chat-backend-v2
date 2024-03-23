package vn.edu.iuh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    private String firstName;
    private String lastName;
    private boolean gender;
    private LocalDate birthday;
    @Length(min = 8, max = 32)
    private String password;
}
