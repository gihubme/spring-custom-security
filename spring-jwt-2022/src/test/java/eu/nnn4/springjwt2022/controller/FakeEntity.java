package eu.nnn4.springjwt2022.controller;

import eu.nnn4.springjwt2022.model.ERole;
import eu.nnn4.springjwt2022.model.Organization;
import eu.nnn4.springjwt2022.model.Role;
import eu.nnn4.springjwt2022.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;
import java.util.UUID;

public class FakeEntity {
    private static PasswordEncoder encoder=new BCryptPasswordEncoder();
    public static String PASS="123123";
    public static Long ind=1L;

    public static Organization getOrganisation(String code){
        code=(code!=null)?code:"code-"+new Random().nextInt(100);
        Organization org=new Organization();
        org.setName(randomStr(5));
        org.setOrganizationAllowsUsers(true);
        org.setActive(true);
        org.setCode(code);
        org.setId(++ind);
        return org;
    }

    public static User getUser(String email,ERole role,String code){
        User user=new User();
        user.setPassword(encoder.encode(PASS));
        user.addRole(new Role(role));
        user.setEmail(email);
        user.setOrganization(getOrganisation(code));
        user.setName(randomStr(5));
        user.setActive(true);
        user.setId(++ind);
        return user;
    }

    public static String randomStr(int length)
    {
        String randomStr = UUID.randomUUID().toString();
        while(randomStr.length() < length) {
            randomStr += UUID.randomUUID().toString();
        }
        return randomStr.substring(0, length);
    }
}
