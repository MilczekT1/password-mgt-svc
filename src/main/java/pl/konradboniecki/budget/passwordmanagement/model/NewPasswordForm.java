package pl.konradboniecki.budget.passwordmanagement.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class NewPasswordForm {

    @NotEmpty(message = "{lostPasswordForm.emailRequired}")
    @Email(message = "{lostPasswordForm.emailRegex}")
    private String email;

    @Getter
    @NotEmpty(message = "{lostPasswordForm.passwordRequired}")
    @Size(min = 6, max = 200, message = "{lostPasswordForm.passwordSize}")
    private String password;

    @NotEmpty(message = "{lostPasswordForm.repeatedPasswordRequired}")
    @Size(min = 6, max = 200, message = "{lostPasswordForm.repeatedPasswordSize}")
    private String repeatedPassword;

    public boolean isRepeatedPasswordTheSame() {
        if (password == null || repeatedPassword == null)
            throw new NullPointerException("Argument should not be null.");
        return password.equals(repeatedPassword);
    }
}
