package pl.konradboniecki.budget.passwordmanagement.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import pl.konradboniecki.budget.passwordmanagement.service.AccountFacade;
import pl.konradboniecki.budget.passwordmanagement.service.NewPasswordRequestService;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.passwordmanagement.integration.AccountServiceIntegrationTests.*;

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
        ids = {STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID + ":" + STUB_VERSION + ":stubs:9001"},
        stubsMode = REMOTE
)
public class AccountServiceIntegrationTests {

    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "account-management";
    public static final String STUB_VERSION = "0.4.0-SNAPSHOT";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AccountFacade accountFacade;
    @MockBean
    private NewPasswordRequestService newPasswordRequestService;

    @BeforeAll
    public void setUp() {
        accountFacade.setFindAccountURL("http://localhost:9001/api/account/");
        doNothing().when(newPasswordRequestService).saveNewPasswordRequest(any());
    }

    @Test
    @DisplayName("Find existing account by email - status code")
    public void givenExistingEmail_whenFindByEmail_thenReturn200() {
        // Given:
        String email = "existing_email@password_management.com";
        // When:
        ResponseEntity<String> accountResponse = accountFacade.getAccountByEmail(email);
        // Then:
        assertThat(accountResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Find existing account by email - body")
    public void givenExistingEmail_whenFindByEmail_thenGetAccountFromBody() throws IOException {
        // Given:
        String email = "existing_email@password_management.com";
        // When:
        ResponseEntity<String> accountResponse = accountFacade.getAccountByEmail(email);
        // Then:
        Map<String, Object> map = new ObjectMapper()
                .readValue(accountResponse.getBody(), new TypeReference<Map<String, Object>>() {
                });
        Assertions.assertAll(
                () -> assertThat(map.get("email").toString()).isEqualTo(email),
                () -> assertThat(map.get("firstName").toString()).isEqualTo("testFirstName"),
                () -> assertThat(map.get("lastName").toString()).isEqualTo("testLastName"),
                () -> assertThat(map.get("id")).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("Find not existing account by email - exception")
    public void givenNotExistingEmail_whenFindByEmail_thenExceptionIsThrown() {
        // Given:
        String email = "not_existing_email@password_management.com";
        // When:
        ResponseStatusException throwable = catchThrowableOfType(() -> accountFacade.getAccountByEmail(email), ResponseStatusException.class);
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        assertThat(throwable.getStatus().is5xxServerError()).isTrue();
        assertThat(throwable.getReason()).contains(email);
    }
}
