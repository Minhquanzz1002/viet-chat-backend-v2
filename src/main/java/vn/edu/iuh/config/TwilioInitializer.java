package vn.edu.iuh.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioInitializer {
    private final static Logger LOGGER = LoggerFactory.getLogger(TwilioInitializer.class);

    private final TwilioProperties twilioProperties;

    public TwilioInitializer(TwilioProperties twilioProperties) {
        this.twilioProperties = twilioProperties;
    }

    @PostConstruct
    public void init() {
        Twilio.init(twilioProperties.getAccountSID(), twilioProperties.getAuthToken());
        LOGGER.info("Twilio initialized with account SID {}", twilioProperties.getAccountSID());
    }

}
