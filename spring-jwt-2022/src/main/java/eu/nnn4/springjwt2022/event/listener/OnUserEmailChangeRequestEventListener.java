package eu.nnn4.springjwt2022.event.listener;

import eu.nnn4.springjwt2022.controller.AppUrlConstants;
import eu.nnn4.springjwt2022.email.EEmailCase;
import eu.nnn4.springjwt2022.email.EmailSenderService;
import eu.nnn4.springjwt2022.event.OnUserEmailChangeRequestEvent;
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
public class OnUserEmailChangeRequestEventListener implements ApplicationListener<OnUserEmailChangeRequestEvent> {
    private static final Logger logger = LoggerFactory.getLogger(OnUserEmailChangeRequestEventListener.class);
    @Value(value = "${server.host}")
    private String host;

    @Autowired
    private EmailSenderService emailSenderService;

    @Override
    public void onApplicationEvent(OnUserEmailChangeRequestEvent event) {
        if (null != event) {
            logger.info(String.format("EmailChangeRequest event received for user [%s]", event.getUserEmail()));
            this.confirmEmailChangeRequest(event);
        }
    }

    private void confirmEmailChangeRequest(OnUserEmailChangeRequestEvent event) {
        Long ms=event.getEmailUpdateExpirationMs()
                .minusMillis(Instant.now().toEpochMilli())
                .toEpochMilli();
        long numberOfHr = new BigDecimal(ms).divide(new BigDecimal(60*60*1000), RoundingMode.UP).longValue();
        String inHr=numberOfHr+"";
        String linkStop = host+"/"+ AppUrlConstants.ECHANGESTOP+"?token="+event.getToken();
        String linkValidate = host+"/"+ AppUrlConstants.ECHANGEVALIDATE+"?token="+event.getToken();
        String[] argsStop={event.getUserName(), linkStop, event.getOrganisationName(),inHr};
        String[] argsValidate={event.getUserName(), linkValidate, event.getOrganisationName(),inHr};

        emailSenderService.sendEmail( event.getUserEmail(),
                EEmailCase.EMAIL_UPDATE_STOP,null,argsStop);
        emailSenderService.sendEmail( event.getNewEmail(),
                EEmailCase.EMAIL_UPDATE_VERIFICATION,null,argsValidate);
    }

}