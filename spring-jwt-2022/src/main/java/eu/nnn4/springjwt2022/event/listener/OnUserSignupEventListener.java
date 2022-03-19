package eu.nnn4.springjwt2022.event.listener;

import eu.nnn4.springjwt2022.controller.AppUrlConstants;
import eu.nnn4.springjwt2022.email.EEmailCase;
import eu.nnn4.springjwt2022.event.OnUserSignupEvent;
import eu.nnn4.springjwt2022.email.EmailSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Configuration
public class OnUserSignupEventListener  implements ApplicationListener<OnUserSignupEvent> {
    private static final Logger logger = LoggerFactory.getLogger(OnUserLogoutSuccessEventListener.class);
    @Value(value = "${server.host}")
    private String host;

    @Autowired
    private EmailSenderService emailSenderService;

    @Override
    public void onApplicationEvent(OnUserSignupEvent event) {
        if (null != event) {
            logger.info(String.format("Signup event received for user [%s]", event.getUserEmail()));
            this.confirmRegistration(event);
        }
    }

    private void confirmRegistration(OnUserSignupEvent event) {
        Long ms=event.getEmailValidationExpirationMs()
                .minusMillis(Instant.now().toEpochMilli())
                .toEpochMilli();
        long numberOfHr = new BigDecimal(ms).divide(new BigDecimal(60*60*1000), RoundingMode.UP).longValue();
        String inHr=numberOfHr+"";
        String link = host+"/"+AppUrlConstants.EMAILVALIDATION+"?token="+event.getToken();
        String[] args={event.getUserName(), link, event.getOrganisationName(),inHr};
        emailSenderService.sendEmail( event.getUserEmail(),
                EEmailCase.EMAIL_VERIFICATION,null,args);
    }
}
