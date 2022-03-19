package eu.nnn4.springjwt2022.event.listener;

import eu.nnn4.springjwt2022.controller.AppUrlConstants;
import eu.nnn4.springjwt2022.email.EEmailCase;
import eu.nnn4.springjwt2022.email.EmailSenderService;
import eu.nnn4.springjwt2022.event.OnUserCreatedByAdminEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;

public class OnUserCreatedByAdminEventListener implements ApplicationListener<OnUserCreatedByAdminEvent> {
    private static final Logger logger = LoggerFactory.getLogger(OnUserLogoutSuccessEventListener.class);

    @Value(value = "${server.host}")
    private String host;
    @Autowired
    private EmailSenderService emailSenderService;

    @Override
    public void onApplicationEvent(OnUserCreatedByAdminEvent event) {
        if (null != event) {
            logger.info(String.format("OnUserCreatedByAdmin event received for user [%s]", event.getUserEmail()));
            this.confirmUserCreatedByAdmin(event);
        }
    }

    private void confirmUserCreatedByAdmin(OnUserCreatedByAdminEvent event) {
        String link=host+"/"+ AppUrlConstants.PUBLIC+"/signin";
        String[] args={event.getUserName(),event.getPassword(),link, event.getOrganisationName()};
        emailSenderService.sendEmail( event.getUserEmail(),
                EEmailCase.USER_CREATED_BY_ADMIN,null,args);
    }
}
