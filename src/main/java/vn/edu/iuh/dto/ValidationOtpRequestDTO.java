package vn.edu.iuh.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationOtpRequestDTO {
    @Pattern(regexp = "^0(?:3[2-9]|8[123458]|7[06789]|5[2689]|9[29])\\d{7}$", message = "Số điện thoại phải là 10 và các đầu số phải thuộc các nhà mạng Viettel (032, 033, 034, 035, 036, 037, 038, 039), Vinaphone (081, 082, 083, 084, 085, 088), MobiFone (070, 076, 077, 078, 079), Vietnamobile (052, 056, 058, 092) và Gmobile (059, 099)")
    private String phone;
    @Length(min = 6, max = 6)
    private String otp;
}
