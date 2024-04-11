package vn.edu.iuh.services.impl;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.config.TwilioProperties;
import vn.edu.iuh.dto.PhoneNumberDTO;
import vn.edu.iuh.dto.OTPResponseDTO;
import vn.edu.iuh.dto.OTPRequestDTO;
import vn.edu.iuh.exceptions.DataExistsException;
import vn.edu.iuh.exceptions.OTPMismatchException;
import vn.edu.iuh.models.RefreshToken;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.enums.RefreshTokenStatus;
import vn.edu.iuh.models.enums.RoleType;
import vn.edu.iuh.models.enums.UserStatus;
import vn.edu.iuh.repositories.OTPRepository;
import vn.edu.iuh.repositories.RefreshTokenRepository;
import vn.edu.iuh.repositories.TwilioSMSRepository;
import vn.edu.iuh.repositories.UserRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.TwilioSMSService;
import vn.edu.iuh.utils.JwtUtil;

import java.text.DecimalFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwilioSMSServiceImpl implements TwilioSMSService {
    private final TwilioSMSRepository twilioSMSRepository;
    private final RefreshTokenRepository refreshTokenRepository;
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
        log.info("Mã OTP của số điện thoại {} là {}", phone, otp);
        return "Gửi OTP thành công";
    }


    @Override
    public OTPResponseDTO verifyOTP(OTPRequestDTO otpRequestDTO) {
//        return twilioSMSRepository.validateOTP(validationOtpRequestDTO.getPhone(), validationOtpRequestDTO.getOtp());
        ;
        boolean isValid = otpRepository.validateOTP(otpRequestDTO.getPhone(), otpRequestDTO.getOtp());
        if (isValid) {
            Optional<User> userOptional = userRepository.findByPhone(otpRequestDTO.getPhone());
            User user = userOptional.orElseGet(() -> userRepository.save(new User(otpRequestDTO.getPhone(), otpRequestDTO.getPhone(), UserStatus.UNVERIFIED, RoleType.USER)));
            RefreshToken refreshToken = RefreshToken
                    .builder()
                    .token(jwtUtil.generateRefreshToken(new UserPrincipal(user)))
                    .status(RefreshTokenStatus.ACTIVE)
                    .user(user)
                    .build();
            refreshTokenRepository.save(refreshToken);
            return OTPResponseDTO.builder()
                    .accessToken(jwtUtil.generateAccessToken(new UserPrincipal(user)))
                    .refreshToken(refreshToken.getToken())
                    .build();
        }
        throw new OTPMismatchException("OTP không chính xác hoặc đã hết hạn");
    }


    @Autowired
    private TwilioProperties twilioConfig;
    Map<String, String> otpMap = new HashMap<>();
    @Override
    public String sendSMSToVerifyV2(PhoneNumberDTO phoneNumberDTO) {
        String otpResponseDto = null;
        try {
            PhoneNumber to = new PhoneNumber(chinhSoPhone(phoneNumberDTO));
            PhoneNumber from = new PhoneNumber(twilioConfig.getPhoneNumberTrial()); // from
            String otp = generateOTP();
            String otpMessage = "Chào bạn, chúng tôi là VietChat. Mã OTP của bạn là  " + otp + ", xin cảm ơn.";
            Message message = Message
                    .creator(to, from,
                            otpMessage)
                    .create();
            otpMap.put(phoneNumberDTO.getPhone(), otp);
            otpResponseDto = otpMessage;
        } catch (Exception e) {
            e.printStackTrace();
            otpResponseDto =  e.getMessage();
        }
        return otpResponseDto;

    }

    @Override
    public OTPResponseDTO verifyOTPV2(OTPRequestDTO otpRequestDTO) {

        Set<String> keys = otpMap.keySet();
        String phone = null;
        for(String key : keys)
            phone = key;
        if (otpRequestDTO.getPhone().equals(phone)) {
            otpMap.remove(phone,otpRequestDTO.getOtp());
            Optional<User> userOptional = userRepository.findByPhone(otpRequestDTO.getPhone());
            User user = userOptional.orElseGet(() -> userRepository.save(new User(otpRequestDTO.getPhone(), otpRequestDTO.getPhone(), UserStatus.UNVERIFIED, RoleType.USER)));
            RefreshToken refreshToken = RefreshToken
                    .builder()
                    .token(jwtUtil.generateRefreshToken(new UserPrincipal(user)))
                    .status(RefreshTokenStatus.ACTIVE)
                    .user(user)
                    .build();
            refreshTokenRepository.save(refreshToken);
            return OTPResponseDTO.builder()
                    .accessToken(jwtUtil.generateAccessToken(new UserPrincipal(user)))
                    .refreshToken(refreshToken.getToken())
                    .build();
        }
        throw new OTPMismatchException("OTP không chính xác hoặc đã hết hạn");

    }
    private String generateOTP() {
        return new DecimalFormat("000000")
                .format(new Random().nextInt(999999));
    }
    public String chinhSoPhone(PhoneNumberDTO phoneNumberDTO) {
        String phone = phoneNumberDTO.getPhone();
        if (phone.startsWith("0")) {
            phone = "+84" + phone.substring(1);
            System.out.println(phone);
        }
        return phone;
    }


}
