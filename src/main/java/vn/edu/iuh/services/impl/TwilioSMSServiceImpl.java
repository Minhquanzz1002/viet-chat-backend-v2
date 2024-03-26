package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.PhoneNumberDTO;
import vn.edu.iuh.dto.ValidateOTPResponseDTO;
import vn.edu.iuh.dto.ValidationOtpRequestDTO;
import vn.edu.iuh.exceptions.DataExistsException;
import vn.edu.iuh.exceptions.OTPMismatchException;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.enums.RoleType;
import vn.edu.iuh.models.enums.UserStatus;
import vn.edu.iuh.repositories.OTPRepository;
import vn.edu.iuh.repositories.TwilioSMSRepository;
import vn.edu.iuh.repositories.UserRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.TwilioSMSService;
import vn.edu.iuh.utils.JwtUtil;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwilioSMSServiceImpl implements TwilioSMSService {
    private final TwilioSMSRepository twilioSMSRepository;
    private final UserRepository userRepository;
    private final OTPRepository otpRepository;
    private final JwtUtil jwtUtil;

    @Override
    public String sendSMSToVerify(PhoneNumberDTO phoneNumberDTO) {
        String phone = phoneNumberDTO.getPhone();
        Optional<User> accountOptional = userRepository.findByPhone(phone);
        if (accountOptional.isPresent() && accountOptional.get().getStatus() != UserStatus.UNVERIFIED) {
            if (accountOptional.get().getStatus() == UserStatus.LOCKED) {
                throw new DataExistsException("Số điện thoại ***" + phone.substring(phone.length() - 3) + " đã bị khóa.");
            }
            throw new DataExistsException("Số điện thoại ***" + phone.substring(phone.length() - 3) + " đã được đăng ký.");
        }
//        boolean result = twilioSMSRepository.sendSMS(phone);
        String otp = otpRepository.generateOTP(phone);
        log.info("Mã OTP của số điện thoại " + phone + " là " + otp);
        return "Gửi OTP thành công";
    }


    @Override
    public ValidateOTPResponseDTO verifyOTP(ValidationOtpRequestDTO validationOtpRequestDTO) {
//        return twilioSMSRepository.validateOTP(validationOtpRequestDTO.getPhone(), validationOtpRequestDTO.getOtp());
        ;
        boolean isValid = otpRepository.validateOTP(validationOtpRequestDTO.getPhone(), validationOtpRequestDTO.getOtp());
        if (isValid) {
            Optional<User> userOptional = userRepository.findByPhone(validationOtpRequestDTO.getPhone());
            User user = userOptional.orElseGet(() -> userRepository.save(new User(validationOtpRequestDTO.getPhone(), validationOtpRequestDTO.getPhone(), UserStatus.UNVERIFIED, RoleType.USER)));
            return ValidateOTPResponseDTO.builder()
                    .accessToken(jwtUtil.generateAccessToken(new UserPrincipal(user)))
                    .refreshToken(jwtUtil.generateRefreshToken(new UserPrincipal(user)))
                    .build();
        } else {
            throw new OTPMismatchException("OTP không chính xác hoặc đã hết hạn");
        }
    }


}
