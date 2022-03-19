# Spring Signin/Signup with Jwt

App serves company/organization accounts that can be added. Organization account has company-user api and can have enabled/disabled user api for its end customers.

There are 3 different roles: `admin`, `companyuser` and `user`. `Companyuser` role can access resources only of her organization.
If organization enabled user api, then `User` role can access only their own resources.

There ara `CustomAuthenticationFilter`, `JwtAuthenticationFilter` and `ApiKeyAuthenticationFilter` filters.
They are added to HttpSecurity filter chain.
- `ApiKeyAuthenticationFilter` extends `OncePerRequestFilter` and is used internally for developer administration and requires `SECRET`. 
- `JwtAuthenticationFilter` extends `OncePerRequestFilter` and verifies validity of an access token that is issued by a login request. If access token has expired then it can be updated via refresh token that is also issued by a login request. By a logout request a current access token is blacklisted in a thread-safe expiring map.
- `CustomAuthenticationFilter` extends `UsernamePasswordAuthenticationFilter` and allows a user to log in with email, password and organization code.

## User Registration
- User submits post request ("/p/signup") with required details. Email validation token is created and OnUserSignupEvent is published. EventListener sends an email with a link to validate the address.
- To activate his new account user opens the validation link ("/p/evalid") before email validation token expiration. 
- If validation link is not valid any more than user requests a new one ("/p/resendValidationEmail").

## User Password Recovery
- User requests password recovery via get ("/p/pforget"). Password forget token is created and OnUserPasswordForgotEvent is published. EventListener sends an email with a link to provide a new password.
- User opens the link before the password forget token expiration. User submits new password via post ("/p/pforget"). After validation OnUserPasswordChangedEvent is published. EventListener sends an email with notification about password change.

## User Password Update
- User is logged in and patches a new password ("/api/updatePassword").
- After validation OnUserPasswordChangedEvent is published. EventListener sends an email with notification about password change.

## User Email Update
- User is logged in and patches a new email ("/api/updateEmail").
- Email validation token is created and OnUserEmailChangeRequestEvent is published. EventListener sends 2 emails with notification about email change. 
- Email to the current address provides a link for canceling email change ("/p/echangeStop").
- Email to the new address provides a link for validation of the address and activation of the email change.


The instruction can be found at:
[Spring Boot Refresh Token with JWT example](http://localhost:8080)

![spring-boot-spring-security-jwt-authentication-flow](spring-boot-spring-security-jwt-authentication-flow.png)
