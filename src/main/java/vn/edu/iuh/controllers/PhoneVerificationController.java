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
import vn.edu.iuh.dto.ValidationOtpRequestDTO;
import vn.edu.iuh.models.Account;
import vn.edu.iuh.repositories.AccountRepository;
import vn.edu.iuh.services.TwilioSMSService;

import java.util.Optional;

/**
 * @author quann
 */

@RestController
@RequestMapping("/v1/verification/otp/sms")
@Tag(name = "Phone verification")
@RequiredArgsConstructor
@Slf4j
public class PhoneVerificationController {
    private final TwilioSMSService twilioSMSService;

    @PostMapping("/send")
    @Operation(summary = "", description = "")
    public ResponseEntity<String> sendOTP(@Valid @RequestBody PhoneNumberDTO phoneNumberDTO) {
        if (twilioSMSService.sendSMSToVerify(phoneNumberDTO)) {
            return ResponseEntity.ok("OTP send successfully");
        }else {
            return ResponseEntity.badRequest().body("OTP send failure.");
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<String> verifyOTP(@Valid @RequestBody ValidationOtpRequestDTO validationOtpRequestDTO) {
        if (twilioSMSService.verifyOTP(validationOtpRequestDTO)) {
            return ResponseEntity.ok("Valid OTP please proceed with your transaction!");
        }else {
            return ResponseEntity.badRequest().body("Invalid otp please retry!");
        }
    }
}
