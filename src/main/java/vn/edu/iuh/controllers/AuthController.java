package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.dto.LoginRequestDTO;
import vn.edu.iuh.dto.RefreshTokenDTO;
import vn.edu.iuh.dto.TokenResponseDTO;
import vn.edu.iuh.dto.RegisterRequestDTO;
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
}
