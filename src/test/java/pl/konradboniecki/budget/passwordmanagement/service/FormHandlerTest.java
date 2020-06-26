package pl.konradboniecki.budget.passwordmanagement.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.passwordmanagement.model.NewPasswordForm;
import pl.konradboniecki.budget.passwordmanagement.model.NewPasswordRequest;
import pl.konradboniecki.chassis.testtools.TestBase;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static pl.konradboniecki.chassis.tools.RestTools.defaultGetHTTPHeaders;

@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = NONE,
        properties = "spring.cloud.config.enabled=false"
)
public class FormHandlerTest extends TestBase {

    @Autowired
    private FormHandler formHandler;
    @MockBean
    private RestTemplate restTemplate;
    @MockBean
    private NewPasswordRequestService newPasswordRequestService;
    private NewPasswordForm validForm;
    private String findAccountByEmailUrl;
    private String mailRequestUrl;
    @Value("${budget.baseUrl.accountManagement}")
    private String BASE_URL;
    @Value("${budget.baseUrl.mail}")
    private String mailBaseUrl;

    @BeforeAll
    public void setUpClass() {
        validForm = new NewPasswordForm();
        validForm.setEmail("test@email.com");
        validForm.setPassword("passwd");
        validForm.setRepeatedPassword("passwd");

        findAccountByEmailUrl = BASE_URL + "/api/account/" + validForm.getEmail() + "?findBy=email";
        mailRequestUrl = mailBaseUrl + "/api/mail/reset-password";

        doNothing().when(newPasswordRequestService).saveNewPasswordRequest(any(NewPasswordRequest.class));
    }

    @BeforeEach
    public void setUpTest() throws FileNotFoundException {
        String accountResponse = getFileContentAsString("controller/AccountResponse", "json");

        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        when(restTemplate.exchange(findAccountByEmailUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class)).thenReturn(new ResponseEntity<>(accountResponse, HttpStatus.OK));

        when(restTemplate.exchange(eq(mailRequestUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok().build());
    }

    @Test
    @DisplayName("Requested account")
    public void givenValidForm_whenHandlingForm_thenRequestedAccount() throws Exception {
        // Given:
        NewPasswordForm newPasswordForm = validForm;
        // When:
        formHandler.handleForm(newPasswordForm);
        // Then:
        verify(restTemplate, times(1))
                .exchange(eq(findAccountByEmailUrl),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(String.class));
    }

    @Test
    @DisplayName("NewPasswordRequest saved in database")
    public void givenValidForm_whenHandlingForm_thenSavedRequestToDatabase() throws Exception {
        // Given:
        NewPasswordForm newPasswordForm = validForm;
        // When:
        formHandler.handleForm(newPasswordForm);
        // Then:
        verify(newPasswordRequestService, times(1))
                .saveNewPasswordRequest(any(NewPasswordRequest.class));
    }

    @Test
    @DisplayName("Requested email with reset code")
    public void givenValidForm_whenHandlingForm_thenEmailRequestHasBeenInvoked() throws Exception {
        // Given:
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class), any(HttpHeaders.class))).thenReturn(ResponseEntity.ok().build());

        NewPasswordForm newPasswordForm = validForm;
        // When:
        formHandler.handleForm(newPasswordForm);
        // Then:
        verify(restTemplate, times(1))
                .exchange(eq(mailRequestUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class));
    }

    @Test
    @DisplayName("Logged all steps during form processing.")
    public void givenValidForm_whenHandlingForm_thenLogAllSteps() throws Exception {
        // Given:
        NewPasswordForm newPasswordForm = validForm;
        // When:
        formHandler.handleForm(newPasswordForm);
        // Then:
        String log = getLog();
        String email = newPasswordForm.getEmail();
        String[] validationMsg = {
                "Beginning has not been logged",
                "Account request has not been logged",
                "NewPasswordRequest has not been saved in database",
                "Mail request has not been sent",
                "End has not been logged"
        };
        assertAll(
                () -> assertTrue(log.matches(".*Start handling form for " + email + ".*"), validationMsg[0]),
                () -> assertTrue(log.matches(".*Sent request for account " + email + ".*"), validationMsg[1]),
                () -> assertTrue(log.matches(".*NewPasswordRequest has been saved for " + email + ".*"), validationMsg[2]),
                () -> assertTrue(log.matches(".*Mail request has been sent for " + email + ".*"), validationMsg[3]),
                () -> assertTrue(log.matches(".*End handling form for " + email + " with success\\..*"), validationMsg[4])
        );
    }
}
