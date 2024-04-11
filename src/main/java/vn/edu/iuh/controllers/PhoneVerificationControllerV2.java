package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.dto.OTPRequestDTO;
import vn.edu.iuh.dto.OTPResponseDTO;
import vn.edu.iuh.dto.PhoneNumberDTO;
import vn.edu.iuh.services.TwilioSMSService;

/**
 * @author quann
 */

@RestController
@RequestMapping("/v2/verification/otp/sms")
@Tag(name = "Phone verification", description = "Xử lý các yêu cầu liên quan đến OTP. Dùng xác nhận số điện thoại để đăng ký tài khoản")
@RequiredArgsConstructor
@Slf4j
public class PhoneVerificationControllerV2 {
    private final TwilioSMSService twilioSMSService;

    @PostMapping("/send")
    @Operation(
            summary = "Gửi mã OTP tới số điện thoại. Dùng xác nhận số điện thoại để đăng ký tài khoản",
            description = """
                    Gửi mã OTP tới số điện thoại để đăng ký tài khoản. Thời hạn của OTP là 5 phút.
                    
                    Lưu ý: Số điện thoại phải là 10 và các đầu số phải thuộc các nhà mạng:
                    - Viettel (032, 033, 034, 035, 036, 037, 038, 039)
                    - Vinaphone (081, 082, 083, 084, 085, 088)
                    - MobiFone (070, 076, 077, 078, 079)
                    - Vietnamobile (052, 056, 058, 092)
                    - Gmobile (059, 099)
                    """
    )
    public String sendOTP(@Valid @RequestBody PhoneNumberDTO phoneNumberDTO) {
        return twilioSMSService.sendSMSToVerifyV2(phoneNumberDTO);
    }

    @PostMapping("/validate")
    @Operation(
            summary = "Xác thực mã OTP",
            description = """
                    Nếu xác thực đúng thì trả về JWT và tạo ra tài khoản trong database với status là UNVERIFIED
                    
                    Gọi tới v1/auth/register để cập nhật lại các thông tin cơ bản
                    
                    Lưu ý: Số điện thoại phải là 10 và các đầu số phải thuộc các nhà mạng:
                    - Viettel (032, 033, 034, 035, 036, 037, 038, 039)
                    - Vinaphone (081, 082, 083, 084, 085, 088)
                    - MobiFone (070, 076, 077, 078, 079)
                    - Vietnamobile (052, 056, 058, 092)
                    - Gmobile (059, 099)
                    """)
    public OTPResponseDTO verifyOTP(@Valid @RequestBody OTPRequestDTO otpRequestDTO) {
        return twilioSMSService.verifyOTPV2(otpRequestDTO);
    }
}
