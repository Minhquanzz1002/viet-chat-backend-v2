package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.PhoneNumberDTO;
import vn.edu.iuh.dto.ValidationOtpRequestDTO;
import vn.edu.iuh.exceptions.DataExistsException;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.enums.UserStatus;
import vn.edu.iuh.repositories.TwilioSMSRepository;
import vn.edu.iuh.repositories.UserRepository;
import vn.edu.iuh.services.TwilioSMSService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwilioSMSServiceImpl implements TwilioSMSService {
    private final TwilioSMSRepository twilioSMSRepository;
    private final UserRepository userRepository;
    @Override
    public boolean sendSMSToVerify(PhoneNumberDTO phoneNumberDTO) {
        String phone = phoneNumberDTO.getPhone();
        Optional<User> account = userRepository.findByPhone(phone);
        if (account.isPresent() && account.get().getStatus() == UserStatus.PHONE_UNVERIFIED) {
            throw new DataExistsException("The phone number " + phone.substring(phone.length() - 3) + " is already registered and verified.");
        }
        boolean result = twilioSMSRepository.sendSMS(phone);
        log.info(String.valueOf(result));
        return result;
    }

    @Override
    public boolean verifyOTP(ValidationOtpRequestDTO validationOtpRequestDTO) {
        return twilioSMSRepository.validateOTP(validationOtpRequestDTO.getPhone(), validationOtpRequestDTO.getOtp());
    }


}
