package vn.edu.iuh.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChangePasswordRequestDTO {

    @NotNull(message = "Mật khẩu mới là bắt buộc")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=])(?=\\S+$).{8,32}$", message = "Mật khẩu từ 8 - 32 ký tự gồm tối thiểu 1 chữ cái viết hoa, 1 chữ cái viết thường, 1 chữ số và 1 ký tự đặc biệt")
    private String passwordold;
    @NotNull(message = "Mật khẩu mới là bắt buộc")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=])(?=\\S+$).{8,32}$", message = "Mật khẩu từ 8 - 32 ký tự gồm tối thiểu 1 chữ cái viết hoa, 1 chữ cái viết thường, 1 chữ số và 1 ký tự đặc biệt")
    private String passwordnew;
    @NotNull(message = "Số điện thoại là bắt buộc")
    @Pattern(regexp = "^0(?:3[2-9]|8[123458]|7[06789]|5[2689]|9[29])\\d{7}$", message = "Số điện thoại phải là 10 và các đầu số phải thuộc các nhà mạng Viettel (032, 033, 034, 035, 036, 037, 038, 039), Vinaphone (081, 082, 083, 084, 085, 088), MobiFone (070, 076, 077, 078, 079), Vietnamobile (052, 056, 058, 092) và Gmobile (059, 099)")
    private String phone;
}
