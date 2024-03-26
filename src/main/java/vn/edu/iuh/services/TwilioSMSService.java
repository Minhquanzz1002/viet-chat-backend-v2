package vn.edu.iuh.services;

import vn.edu.iuh.dto.PhoneNumberDTO;
import vn.edu.iuh.dto.ValidateOTPResponseDTO;
import vn.edu.iuh.dto.ValidationOtpRequestDTO;

public interface TwilioSMSService {
    String sendSMSToVerify(PhoneNumberDTO phoneNumberDTO);
    ValidateOTPResponseDTO verifyOTP(ValidationOtpRequestDTO validationOtpRequestDTO);
}
