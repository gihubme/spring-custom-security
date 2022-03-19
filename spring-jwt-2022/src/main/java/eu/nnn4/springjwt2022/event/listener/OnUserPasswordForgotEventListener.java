package eu.nnn4.springjwt2022.event.listener;

import eu.nnn4.springjwt2022.controller.AppUrlConstants;
import eu.nnn4.springjwt2022.email.EEmailCase;
import eu.nnn4.springjwt2022.email.EmailSenderService;
import eu.nnn4.springjwt2022.event.OnUserPasswordForgotEvent;
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
public class OnUserPasswordForgotEventListener implements ApplicationListener<OnUserPasswordForgotEvent> {
    private static final Logger logger = LoggerFactory.getLogger(OnUserLogoutSuccessEventListener.class);
    @Value(value = "${server.host}")
    private String host;

    @Autowired
    private EmailSenderService emailSenderService;

    @Override
    public void onApplicationEvent(OnUserPasswordForgotEvent event) {
        if (null != event) {
            logger.info(String.format("PasswordForgot event received for user [%s]", event.getUserEmail()));
            this.confirmPasswordForgotRequest(event);
        }
    }

    private void confirmPasswordForgotRequest(OnUserPasswordForgotEvent event) {
        Long ms=event.getPasswordForgetExpirationMs()
                .minusMillis(Instant.now().toEpochMilli())
                .toEpochMilli();
        long numberOfHr = new BigDecimal(ms).divide(new BigDecimal(60*60*1000), RoundingMode.UP).longValue();
        String inHr=numberOfHr+"";
        String link = host+"/"+AppUrlConstants.PASSWORDFORGET+"?token="+event.getToken();
        String[] args={event.getUserName(), link, event.getOrganisationName(),inHr};
        emailSenderService.sendEmail( event.getUserEmail(),
                EEmailCase.PASSWORD_FORGET,null,args);
    }
}
