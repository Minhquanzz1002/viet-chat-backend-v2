package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.dto.LoginRequestDTO;
import vn.edu.iuh.dto.LoginResponseDTO;
import vn.edu.iuh.dto.RegisterRequestDTO;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.enums.UserStatus;
import vn.edu.iuh.services.AuthService;
import vn.edu.iuh.services.UserService;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "Xác thực người dùng")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "Đăng ký tài khoản")
    @PostMapping("/register")
    public String register(@Valid RegisterRequestDTO registerRequestDTO) {
        User user = userService.findByPhone(registerRequestDTO.getPhone());
        if (user.getStatus() == UserStatus.PHONE_UNVERIFIED){
            return "Registration failure!";
        }else{
            user.setPassword(registerRequestDTO.getPassword());
            userService.save(user);
            return "Registered successfully!";
        }
    }

    @Operation(summary = "Xử lý yêu cầu đăng nhập", description = "Đăng nhập và trả về JWT")
    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        return authService.login(loginRequestDTO);
    }
}
