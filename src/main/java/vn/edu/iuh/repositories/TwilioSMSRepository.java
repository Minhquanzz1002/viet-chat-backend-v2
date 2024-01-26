package vn.edu.iuh.repositories;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.config.TwilioProperties;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
@Repository
@RequiredArgsConstructor
@Slf4j
public class TwilioSMSRepository {
    private final TwilioProperties twilioProperties;
    Map<String, String> otps = new HashMap<>();
    public boolean sendSMS(String phone) {
        try {
            PhoneNumber from = new PhoneNumber(twilioProperties.getPhoneNumberTrial());
            PhoneNumber to = new PhoneNumber(phone);
            String otp = generateOTP();
            String message = "Mã xác thực của bạn là: " + otp;
            Message.creator(to, from, message).create();
            otps.put(phone, otp);
            return true;
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
    }

    public boolean validateOTP(String phone, String otp) {
        if (otp.equals(otps.get(phone))) {
            otps.remove(phone);
            return true;
        }
        return false;
    }


    private String generateOTP() {
        return new DecimalFormat("000000").format(new Random().nextInt(999999));
    }
}
