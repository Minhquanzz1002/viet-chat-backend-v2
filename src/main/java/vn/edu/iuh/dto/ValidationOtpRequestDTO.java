package vn.edu.iuh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationOtpRequestDTO {
//    @Length(min = 10, max = 10)
    private String phone;
    @Length(min = 6, max = 6)
    private String otp;
}
