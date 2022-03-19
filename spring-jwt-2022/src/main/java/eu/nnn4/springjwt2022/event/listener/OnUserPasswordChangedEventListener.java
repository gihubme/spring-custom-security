package eu.nnn4.springjwt2022.event.listener;

import eu.nnn4.springjwt2022.controller.AppUrlConstants;
import eu.nnn4.springjwt2022.email.EEmailCase;
import eu.nnn4.springjwt2022.email.EmailSenderService;
import eu.nnn4.springjwt2022.event.OnUserPasswordChangedEvent;
import eu.nnn4.springjwt2022.event.OnUserSignupEvent;
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
public class OnUserPasswordChangedEventListener implements ApplicationListener<OnUserPasswordChangedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(OnUserLogoutSuccessEventListener.class);

    @Autowired
    private EmailSenderService emailSenderService;

    @Override
    public void onApplicationEvent(OnUserPasswordChangedEvent event) {
        if (null != event) {
            logger.info(String.format("PasswordChanged event received for user [%s]", event.getUserEmail()));
            this.confirmPasswordChange(event);
        }
    }

    private void confirmPasswordChange(OnUserPasswordChangedEvent event) {
        String[] args={event.getUserName(),event.getOrganisationName()};
        emailSenderService.sendEmail( event.getUserEmail(),
                EEmailCase.PASSWORD_UPDATE,null,args);
    }
}
