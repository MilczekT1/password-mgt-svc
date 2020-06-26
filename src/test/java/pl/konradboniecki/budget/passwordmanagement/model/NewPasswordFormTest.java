package pl.konradboniecki.budget.passwordmanagement.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.*;

public class NewPasswordFormTest {

    @Test
    @DisplayName("All properties are null by default")
    public void givenNoArguments_whenCreateObj_thenAllPropertiesAreNulls() {
        // When:
        NewPasswordForm newPasswordForm = new NewPasswordForm();
        assertAll(
                () -> assertNull(newPasswordForm.getEmail()),
                () -> assertNull(newPasswordForm.getPassword()),
                () -> assertNull(newPasswordForm.getRepeatedPassword())
        );
    }

    @Test
    @DisplayName("Throw NullPointerException if any password is null")
    public void givenNullPasswords_whenCompare_thenThrowNullPointerException() {
        // Given:
        NewPasswordForm newPasswordForm1 = new NewPasswordForm();
        NewPasswordForm newPasswordForm2 = new NewPasswordForm();
        // When:
        newPasswordForm1.setPassword("passwd");
        newPasswordForm2.setRepeatedPassword("passwd");
        // Then:
        assertAll(
                () -> assertThrows(NullPointerException.class, newPasswordForm1::isRepeatedPasswordTheSame),
                () -> assertThrows(NullPointerException.class, newPasswordForm2::isRepeatedPasswordTheSame)
        );
    }

    @Test
    @DisplayName("Log message if any password is null")
    public void givenNullPasswords_whenCompare_thenLogMessage() {
        // Given:
        NewPasswordForm newPasswordForm1 = new NewPasswordForm();
        NewPasswordForm newPasswordForm2 = new NewPasswordForm();
        newPasswordForm1.setPassword("passwd");
        newPasswordForm2.setRepeatedPassword("passwd");
        String exceptionMsg = "Argument should not be null.";

        // When:
        NullPointerException throwable1 = catchThrowableOfType(newPasswordForm1::isRepeatedPasswordTheSame, NullPointerException.class);
        NullPointerException throwable2 = catchThrowableOfType(newPasswordForm2::isRepeatedPasswordTheSame, NullPointerException.class);

        // Then:
        assertThat(throwable1.getMessage()).contains(exceptionMsg);
        assertThat(throwable2.getMessage()).contains(exceptionMsg);
    }


    @Test
    @DisplayName("Return true if password match repeated password")
    public void givenTheSameRepeatedPassword_whenCheckIfPasswordsAreEqual_thenReturnTrue() {
        // Given:
        NewPasswordForm newPasswordForm = new NewPasswordForm();
        // When:
        newPasswordForm.setPassword("pass");
        newPasswordForm.setRepeatedPassword("pass");
        // Then:
        assertTrue(newPasswordForm.isRepeatedPasswordTheSame());
    }

    @Test
    @DisplayName("Return false if password doesn't match repeated password")
    public void givenDifferentSameRepeatedPassword_whenCheckIfPasswordsAreEqual_thenReturnFalse() {
        // Given:
        NewPasswordForm newPasswordForm = new NewPasswordForm();
        // When:
        newPasswordForm.setPassword("pass");
        newPasswordForm.setRepeatedPassword("something different");
        // Then:
        assertFalse(newPasswordForm.isRepeatedPasswordTheSame());
    }
}
