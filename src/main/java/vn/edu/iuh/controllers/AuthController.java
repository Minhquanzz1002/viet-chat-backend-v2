package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.dto.RegisterRequestDTO;
import vn.edu.iuh.models.Account;
import vn.edu.iuh.services.AccountService;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication")
@RequiredArgsConstructor
public class AuthController {
    private final AccountService accountService;

    @PostMapping("/register")
    public String register(@Valid RegisterRequestDTO registerRequestDTO) {
        Account account = accountService.findByPhone(registerRequestDTO.getPhone());
        if (!account.isPhoneVerified()){
            return "Registration failure!";
        }else{
            account.setPassword(registerRequestDTO.getPassword());
            accountService.save(account);
            return "Registered successfully!";
        }
    }
}
