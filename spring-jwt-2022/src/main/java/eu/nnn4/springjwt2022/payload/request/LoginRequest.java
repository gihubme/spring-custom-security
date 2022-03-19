package eu.nnn4.springjwt2022.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest extends IdRelatedRequest{
 
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
    public LoginRequest(String pass, String email,String code){
        super(email,code);
        this.password=pass;
    }
}
