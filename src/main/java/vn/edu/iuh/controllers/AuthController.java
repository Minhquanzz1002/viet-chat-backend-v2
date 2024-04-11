package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.dto.*;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.AuthService;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "Xác thực người dùng")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Đăng ký tài khoản",
            description = """
                    Cập nhật mật khẩu và các thông tin cơ bản sau khi số điện thoại đã được xác thực
                    """
    )
    @PostMapping("/register")
    public String register(@RequestBody @Valid RegisterRequestDTO registerRequestDTO, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return authService.register(registerRequestDTO, userPrincipal);
    }

    @Operation(
            summary = "Xử lý yêu cầu đăng nhập",
            description = """
                    Đăng nhập và trả về access (30 phút) và refresh token (14 ngày)
                    """
    )
    @PostMapping("/login")
    public TokenResponseDTO login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        return authService.login(loginRequestDTO);
    }

    @Operation(
            summary = "Lấy access token mới", description = """
            Lấy access token mới bằng refresh token
            """
    )
    @PostMapping("/refresh-token")
    public TokenResponseDTO getAccessToken(@RequestBody @Valid RefreshTokenDTO refreshTokenDTO) {
        return authService.getAccessToken(refreshTokenDTO.getToken());
    }

    @Operation(
            summary = "Đăng xuất trên tất cả thiết bị khác", description = """
            Xóa toàn bị refresh token đang ACTIVE trừ token của thiết bị hiện tại
             """
    )
    @PostMapping("/logout/all")
    public String logoutAll(@RequestBody @Valid RefreshTokenDTO refreshTokenDTO) {
        return authService.logoutAll(refreshTokenDTO.getToken());
    }

    @Operation(
            summary = "Đăng xuất khỏi thiết bị hiện tại", description = """
            Xóa refresh token của thiết bị hiện tại
             """
    )
    @PostMapping("/logout")
    public String logout(@RequestBody @Valid RefreshTokenDTO refreshTokenDTO) {
        return authService.logout(refreshTokenDTO.getToken());
    }

    @Operation(
            summary = "Gửi yêu cầu quên mật khẩu", description = """
            Gửi yêu cầu quên mật khẩu và nhận OTP
             """
    )
    @PostMapping("/password/forgot")
    public String forgotPassword(@RequestBody @Valid PhoneNumberDTO phoneNumberDTO) {
        return authService.forgotPassword(phoneNumberDTO.getPhone());
    }

    @Operation(
            summary = "Xác thực OTP cho quá trình lấy lại mật khẩu",
            description = """
                    Xác thực OTP để lấy lại mật khẩu. Nếu OTP hợp lệ hệ thống trả về 1 Reset Token có thời hạn 5 phút. Dùng Reset token để cập nhật lại mật khẩu tại /v1/auth/password/reset
                    """
    )
    @PostMapping("/password/reset/validate")
    public ResetTokenDTO validateResetPasswordOTP(@RequestBody @Valid OTPRequestDTO otpRequestDTO) {
        return authService.validateResetPassword(otpRequestDTO.getPhone(), otpRequestDTO.getOtp());
    }

    @Operation(
            summary = "Cập nhật mật khẩu sau khi xác thực OTP",
            description = """
            Cập nhật mật khẩu mới sau khi xác thực OTP
             """
    )
    @PostMapping("/password/reset")
    public String resetPassword(@RequestBody @Valid ResetPasswordRequestDTO resetPasswordRequestDTO) {
        return authService.resetPassword(resetPasswordRequestDTO.getToken(), resetPasswordRequestDTO.getPassword());
    }
    @PostMapping("/password/change")
    public String changePassword(@RequestBody @Valid ChangePasswordRequestDTO changePasswordRequestDTO) {
        return authService.changePassword(changePasswordRequestDTO.getPasswordold(), changePasswordRequestDTO.getPasswordnew(),changePasswordRequestDTO.getPhone());
    }
}
