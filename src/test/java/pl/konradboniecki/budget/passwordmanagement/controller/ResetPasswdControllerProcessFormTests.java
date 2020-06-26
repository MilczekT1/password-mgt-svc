package pl.konradboniecki.budget.passwordmanagement.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import pl.konradboniecki.budget.passwordmanagement.model.NewPasswordForm;
import pl.konradboniecki.budget.passwordmanagement.service.FormHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static pl.konradboniecki.budget.passwordmanagement.model.ViewTemplate.LOST_PASSWORD_FORM;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = MOCK,
        properties = "spring.cloud.config.enabled=false"
)
public class ResetPasswdControllerProcessFormTests {

    @Autowired
    private ResetPasswdController controller;
    @MockBean
    private FormHandler formHandler;

    private NewPasswordForm validForm;
    private BindingResult bindingResult;
    @Value("${budget.baseUrl.gateway}")
    private String BASE_URL;

    @BeforeAll
    public void setUp() {
        String passwd = "password";
        validForm = new NewPasswordForm();
        validForm.setEmail("email@adress.com");
        validForm.setPassword(passwd);
        validForm.setRepeatedPassword(passwd);

        bindingResult = mock(BindingResult.class);
    }

    @Test
    @DisplayName("Return form view if form is invalid")
    public void givenInvalidForm_whenProcessForm_thenReturnFormView() throws Exception {
        // Given:
        NewPasswordForm validForm = this.validForm;
        when(bindingResult.hasErrors()).thenReturn(true);
        // When:
        ModelAndView modelAndView = controller.processForm(validForm, bindingResult);
        // Then:
        assertThat(modelAndView.getViewName()).isEqualTo(LOST_PASSWORD_FORM.getViewName());
    }

    @Test
    @DisplayName("Return form view with error flag if repeated password does not match")
    public void givenInvalidRepeatedPasswd_whenProcessForm_thenReturnFormViewWithFailFlag() throws Exception {
        // Given:
        NewPasswordForm validForm = new NewPasswordForm();
        validForm.setPassword("123");
        validForm.setRepeatedPassword("1234");
        when(bindingResult.hasErrors()).thenReturn(false);
        // When:
        ModelAndView modelAndView = controller.processForm(validForm, bindingResult);
        // Then:
        assertThat(modelAndView.getModel().get("repeatedPasswordFailure")).isEqualTo(true);
    }

    @Test
    @DisplayName("Return ModelAndView with redirect to main page")
    public void givenValidParams_whenProcessForm_thenRedirectToMainPAge() throws Exception {
        // Given:
        NewPasswordForm validForm = this.validForm;
        when(bindingResult.hasErrors()).thenReturn(false);
        doNothing().when(formHandler).handleForm(validForm);
        // When:
        ModelAndView modelAndView = controller.processForm(validForm, bindingResult);
        // Then:
        assertThat(modelAndView.getViewName()).isEqualTo("redirect:" + BASE_URL);
    }

    @Test
    @DisplayName("Form has been handled")
    public void givenValidParams_whenProcessForm_thenFormHasBeenHandled() throws Exception {
        // Given:
        NewPasswordForm validForm = this.validForm;
        when(bindingResult.hasErrors()).thenReturn(false);
        doNothing().when(formHandler).handleForm(validForm);
        // When:
        controller.processForm(validForm, bindingResult);
        // Then:
        verify(formHandler, times(1)).handleForm(any(NewPasswordForm.class));
    }
}
