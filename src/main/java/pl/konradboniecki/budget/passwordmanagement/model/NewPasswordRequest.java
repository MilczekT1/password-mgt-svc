package pl.konradboniecki.budget.passwordmanagement.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import pl.konradboniecki.chassis.tools.HashGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "new_password")
public class NewPasswordRequest {

    @Id
    @Column(name = "account_id")
    private Long accountId;
    @Column(name = "new_password")
    private String newPassword;
    @Column(name = "apply_time")
    private ZonedDateTime applyTime;
    @Column(name = "reset_code")
    private String resetCode;

    public NewPasswordRequest(NewPasswordForm newPasswordForm) {
        setApplyTime(ZonedDateTime.now());
        setNewPassword(newPasswordForm.getPassword());
    }

    public NewPasswordRequest(NewPasswordForm newPasswordForm, Long accountId, String resetCode) {
        this(newPasswordForm);
        setAccountId(accountId);
        setResetCode(resetCode);
    }

    public void setNewPassword(String password) {
        this.newPassword = new HashGenerator().hashPassword(password);
    }
}
