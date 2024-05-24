package vn.edu.iuh.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "twilio")
@Data
public class TwilioProperties {
    private String accountSID;
    private String authToken;
    private String phoneNumberTrial;
    private String toPhoneNumber;
}
