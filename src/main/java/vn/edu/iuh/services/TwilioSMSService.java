package vn.edu.iuh.services;

import vn.edu.iuh.dto.PhoneNumberDTO;
import vn.edu.iuh.dto.OTPResponseDTO;
import vn.edu.iuh.dto.OTPRequestDTO;

public interface TwilioSMSService {
    String sendSMSToVerify(PhoneNumberDTO phoneNumberDTO);
    OTPResponseDTO verifyOTP(OTPRequestDTO otpRequestDTO);
    String sendSMSToVerifyV2(PhoneNumberDTO phoneNumberDTO);
    OTPResponseDTO verifyOTPV2(OTPRequestDTO otpRequestDTO);


}
