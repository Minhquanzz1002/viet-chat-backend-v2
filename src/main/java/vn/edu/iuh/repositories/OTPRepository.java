package vn.edu.iuh.repositories;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
@Slf4j
@Getter
public class OTPRepository {
    private Map<String, Pair<String, Date>> otps = new HashMap<>();
    private final int OTP_EXPIRY_SECONDS = 5*60*1000;

    public String generateOTP(String phone) {
        Random random = new Random();
        String otp = String.valueOf(random.nextInt(10)) +
                random.nextInt(10) +
                random.nextInt(10) +
                random.nextInt(10) +
                random.nextInt(10) +
                random.nextInt(10);
        log.info(new Date(System.currentTimeMillis()).toString());
        otps.put(phone, Pair.of(otp, new Date(System.currentTimeMillis() + OTP_EXPIRY_SECONDS)));
        log.info(otps.toString());
        return otp;
    }

    public boolean validateOTP(String phone, String otp) {
        log.info(otps.toString());
        Pair<String, Date> otpPair = otps.get(phone);
        if (otpPair != null) {
            String storedOTP = otpPair.getFirst();
            Date expiryDate = otpPair.getSecond();
            if (storedOTP.equals(otp) && new Date().before(expiryDate)) {
                otps.remove(phone);
                return true;
            }
        }
        return false;
    }
}
