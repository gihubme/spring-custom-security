package eu.nnn4.springjwt2022.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class EmailSenderService {

    @Autowired
    public EmailSenderService(JavaMailSender javaMailSender, MessageSource messages){
        this.javaMailSender=javaMailSender;
        this.messages=messages;
    }
    private JavaMailSender javaMailSender;
    private MessageSource messages;

    @Async
    public void sendEmail(String userEmail, EEmailCase prefix, Locale locale, String[] args) {
        EmailBase base=new EmailBase(messages,prefix,locale);
        SimpleMailMessage simpleEmail=base.createEmail(userEmail, args);

        javaMailSender.send(simpleEmail);
    }
}