package vn.edu.iuh.services;

import vn.edu.iuh.dto.PhoneNumberDTO;
import vn.edu.iuh.dto.ValidationOtpRequestDTO;

public interface TwilioSMSService {
    boolean sendSMSToVerify(PhoneNumberDTO phoneNumberDTO);
    boolean verifyOTP(ValidationOtpRequestDTO validationOtpRequestDTO);
}
