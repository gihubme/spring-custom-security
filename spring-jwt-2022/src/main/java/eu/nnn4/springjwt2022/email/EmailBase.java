package eu.nnn4.springjwt2022.email;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.SimpleMailMessage;

import java.util.Locale;


public class EmailBase {
    private MessageSource messageSource;
    private EEmailCase prefix;
    private Locale locale;

    public EmailBase(MessageSource messageSource, EEmailCase prefix, Locale locale){
        this.messageSource=messageSource;
        this.prefix=prefix;
        this.locale=(locale==null)?LocaleContextHolder.getLocale():locale;
    }

    public SimpleMailMessage createEmail(String userEmail,String[] args){
        Integer requiredNum=getArgNum();
        if(args.length<requiredNum)
            throw new IllegalArgumentException("Couldn't create email body. " +
                    "Less then "+ requiredNum +" arguments provided for "+prefix.getEmailCase());
        SimpleMailMessage simpleMail=new SimpleMailMessage();
        simpleMail.setText(getBody(args));
        simpleMail.setTo(userEmail);
        simpleMail.setSubject(getSubject());
        simpleMail.setFrom(getFrom());
        return simpleMail;
    }

    private String getBody(String[] args){
        Locale loc=LocaleContextHolder.getLocale();
        String body=null,greeting=null,signature=null,smallPrint=null;
        String prefixS=prefix.getEmailCase();
        switch (prefix) {
            case PASSWORD_FORGET:
            case EMAIL_VERIFICATION:
                greeting=messageSource.getMessage(prefixS+".greeting", new Object[]{args[0]},
                        "Hello,",locale);
                body= messageSource.getMessage(prefixS+".body", new Object[]{args[1]},
                        null,locale);
                signature=messageSource.getMessage(prefixS+".signature", new Object[]{args[2]},
                        "Your support team.",locale);
                smallPrint=messageSource.getMessage(prefixS+".smallPrint", new Object[]{args[3]},
                        "Your support team.",locale);
                break;
            case USER_CREATED_BY_ADMIN:
                greeting=messageSource.getMessage(prefixS+".greeting", new Object[]{args[0]},
                        "Hello,",locale);
                body= messageSource.getMessage(prefixS+".body", new Object[]{args[1],args[2]},
                        null,locale);
                signature=messageSource.getMessage(prefixS+".signature", new Object[]{args[3]},
                        "Your support team.",locale);
                break;
            case PASSWORD_UPDATE:
                greeting=messageSource.getMessage(prefixS+".greeting", new Object[]{args[0]},
                        "Hello,",locale);
                body= messageSource.getMessage(prefixS+".body", null,
                        null,locale);
                signature=messageSource.getMessage(prefixS+".signature", new Object[]{args[1]},
                        "Your support team.",locale);
                break;
            default:
                throw new IllegalArgumentException("Couldn't create email body. No valid email case were provided.");
        }

        if(body==null || body.isBlank())
            throw new IllegalArgumentException("Couldn't create email body.");

        return new StringBuilder().append(greeting!=null?greeting+"\n":"")
                .append(body)
                .append(signature!=null?"\n\n"+signature:"")
                .append(smallPrint!=null?"\n\n"+smallPrint:"").toString();

    }

    private String getSubject(){
        String sub= messageSource.getMessage(prefix.getEmailCase()+".subject", null,
                "Your support team",locale);
        if(sub==null || sub.isBlank())
            throw new IllegalArgumentException("Couldn't create email subject.");
        return sub;
    }

    private String getFrom(){
        String from= messageSource.getMessage(prefix.getEmailCase()+".from", null,
                null,locale);
        if(from==null || from.isBlank())
            throw new IllegalArgumentException("Couldn't populate email `from` field.");
        return from;
    }

    private Integer getArgNum(){
        String numS= messageSource.getMessage(prefix.getEmailCase()+".argNum", null,
                null,locale);
        Integer num;
        try{
            num=Integer.parseInt(numS);
        }catch (Exception ex){
            throw new IllegalArgumentException("Couldn't create email. Wrong number of arguments.");
        }
        return num;
    }

}
