package eu.nnn4.springjwt2022.controller;

public class AppUrlConstants {
    public final static String USER_URL="u";
    public final static String COMPANYUSER_URL="c";
    public final static String DEVADMIN_URL="d";

    public final static String API="api";

    public final static String ROLEMAP="roleMap";
    public final static String COMPANYCODE="code";
    public final static String PUBLIC="p";

    public final static String EVALID="/evalid";
    public final static String EMAILVALIDATION=PUBLIC+EVALID;
    public final static String PFORGET="/pforget";
    public final static String PASSWORDFORGET=PUBLIC+PFORGET;

    public final static String ECHANGESTOP="/echangeStop";
    public final static String EMAILUPDATESTOP=PUBLIC+ECHANGESTOP;
    public final static String ECHANGEVALIDATE="/echangeValidate";
    public final static String EMAILUPDATE=PUBLIC+ECHANGEVALIDATE;
}
/*
"localhost:8080/d/organizations" >> ?code=aa
"localhost:8080/d/organizations/activate" << organizationId
"localhost:8080/d/organizations/deactivate" << organizationId
"localhost:8080/d/organizations/create"

"localhost:8080/d/users" >> ?role=xx&code=zz
"localhost:8080/d/users/create"
"localhost:8080/d/users/deactivate" << userId
"localhost:8080/d/users/activate" << userId
"localhost:8080/d/users/lock" << userId
"localhost:8080/d/users/unlock" << userId

=====================================
"localhost:8080/p/signup" << Post
"localhost:8080/p/signin" << Post
"localhost:8080/p/refresh" << Post
"localhost:8080/p/pforget" << Get & Post
"localhost:8080/p/evalig" << Get
"localhost:8080/p/resendValidationEmail" << Get
"localhost:8080/p/echangeStop" << Get
"localhost:8080/p/echangeValidate" << Patch
"localhost:8080/p/emailAvailability?email=zz&&code=ww" << Get
=====================================
"localhost:8080/api/me"
"localhost:8080/api/logout"
"localhost:8080/api/updateEmail"
"localhost:8080/api/updatePassword"
=====================================
"localhost:8080/api/code/c|u/products"
=====================================

*/
