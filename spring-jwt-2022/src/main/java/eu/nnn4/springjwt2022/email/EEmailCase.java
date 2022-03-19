package eu.nnn4.springjwt2022.email;

public enum EEmailCase {
    EMAIL_VERIFICATION("emailVerification"),
    EMAIL_UPDATE_STOP("emailUpdateStop"),
    EMAIL_UPDATE_VERIFICATION("emailUpdateVerification"),
    USER_CREATED_BY_ADMIN("onUserCreatedByAdmin"),
//    ACCOUNT_ACTIVATION("accountActivation"),
//    ACCOUNT_DEACTIVATION("accountDeactivation"),
    PASSWORD_UPDATE("passwordChanged"),
    PASSWORD_FORGET("passwordForget");

    private final String emailCase;

    EEmailCase(String emailCase) {
        this.emailCase=emailCase;
    }
    public String getEmailCase() {
        return emailCase;
    }
}
