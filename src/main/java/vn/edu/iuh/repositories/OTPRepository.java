package vn.edu.iuh.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
@Slf4j
public class OTPRepository {
    private Map<String, Pair<String, Date>> otps = new HashMap<>();
    private final int OTP_EXPIRY_SECONDS = 18000;

    public String generateOTP(String phone) {
        Random random = new Random();
        String otp = String.valueOf(random.nextInt(10)) +
                random.nextInt(10) +
                random.nextInt(10) +
                random.nextInt(10) +
                random.nextInt(10) +
                random.nextInt(10);
        otps.put(phone, Pair.of(otp, new Date(System.currentTimeMillis() + OTP_EXPIRY_SECONDS)));
        log.info(otps.toString());
        return otp;
    }

    public boolean validateOTP(String phone, String otp) {
        Pair<String, Date> otpPair = otps.get(phone);
        if (otpPair != null) {
            log.info(otpPair.toString());
            String storedOTP = otpPair.getFirst();
            Date expiryDate = otpPair.getSecond();
            return storedOTP.equals(otp) && new Date().before(expiryDate);
        }
        return false;
    }
}
