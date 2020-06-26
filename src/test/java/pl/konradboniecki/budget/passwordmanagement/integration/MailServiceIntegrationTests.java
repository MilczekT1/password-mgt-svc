package pl.konradboniecki.budget.passwordmanagement.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import pl.konradboniecki.budget.passwordmanagement.model.json.Account;
import pl.konradboniecki.budget.passwordmanagement.model.json.ActivationLinkRequest;
import pl.konradboniecki.budget.passwordmanagement.service.MailHandler;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.passwordmanagement.integration.MailServiceIntegrationTests.*;

@ExtendWith(SpringExtension.class)
@TestInstance(PER_CLASS)
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {
                "spring.cloud.config.enabled=false",
                "stubrunner.cloud.loadbalancer.enabled=false"
        }
)
@AutoConfigureStubRunner(
        repositoryRoot = "http://77.55.214.60:5001/repository/maven-public/",
        ids = {STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID + ":" + STUB_VERSION + ":stubs:9000"},
        stubsMode = REMOTE
)
public class MailServiceIntegrationTests {

    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "mail";
    public static final String STUB_VERSION = "0.4.0-SNAPSHOT";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MailHandler mailHandler;

    private Account validAccount;
    private Account invalidAccount;
    private String resetCode;

    @BeforeAll
    public void setUp() {
        mailHandler.setResetPasswordMailUrl("http://localhost:9000/api/mail/reset-password");
        validAccount = new Account()
                .setEmail("test@mail.com")
                .setFirstName("kon")
                .setLastName("bon")
                .setId(2L);
        invalidAccount = new Account()
                .setEmail("test@mail.com")
                .setFirstName("kon")
                .setLastName("bon")
                .setId(1L);
        resetCode = "29431ce1-8282-4489-8dd9-50f91e4c5653";
    }

    @Test
    @DisplayName("Check mail service integration")
    public void givenValidInput_whenRequestMailService_thenNoException() {
        //Given:
        ActivationLinkRequest activationLinkRequest = new ActivationLinkRequest()
                .setAccount(validAccount)
                .setResetCode(resetCode);
        // When:
        Throwable throwable = catchThrowable(() -> mailHandler.requestMailWithResetCode(activationLinkRequest));
        // Then:
        assertThat(throwable).isNull();
    }

    @Test
    @DisplayName("Check mail service failure throws exception ")
    public void givenInvalidInput_whenRequestMailService_thenExceptionIsThrown() {
        //Given:
        ActivationLinkRequest activationLinkRequest = new ActivationLinkRequest()
                .setAccount(invalidAccount)
                .setResetCode(resetCode);
        // When:
        ResponseStatusException throwable = catchThrowableOfType(() -> mailHandler.requestMailWithResetCode(activationLinkRequest),
                ResponseStatusException.class);
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        assertThat(throwable.getStatus().is5xxServerError()).isTrue();
    }

}
