package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.dto.PhoneNumberDTO;
import vn.edu.iuh.dto.ValidateOTPResponseDTO;
import vn.edu.iuh.dto.ValidationOtpRequestDTO;
import vn.edu.iuh.services.TwilioSMSService;

/**
 * @author quann
 */

@RestController
@RequestMapping("/v1/verification/otp/sms")
@Tag(name = "Phone verification", description = "Xử lý các yêu cầu liên quan đến OTP. Dùng xác nhận số điện thoại để đăng ký tài khoản")
@RequiredArgsConstructor
@Slf4j
public class PhoneVerificationController {
    private final TwilioSMSService twilioSMSService;

    @PostMapping("/send")
    @Operation(
            summary = "Gửi mã OTP tới số điện thoại. Dùng xác nhận số điện thoại để đăng ký tài khoản",
            description = """
                    Gửi mã OTP tới số điện thoại để đăng ký tài khoản. Thời hạn của OTP là 5 phút
                    """
    )
    public String sendOTP(@Valid @RequestBody PhoneNumberDTO phoneNumberDTO) {
        return twilioSMSService.sendSMSToVerify(phoneNumberDTO);
    }

    @PostMapping("/validate")
    @Operation(summary = "Xác thực mã OTP", description = "")
    public ValidateOTPResponseDTO verifyOTP(@Valid @RequestBody ValidationOtpRequestDTO validationOtpRequestDTO) {
        return twilioSMSService.verifyOTP(validationOtpRequestDTO);
    }
}
