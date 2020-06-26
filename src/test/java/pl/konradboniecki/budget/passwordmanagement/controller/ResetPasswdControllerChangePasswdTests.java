package pl.konradboniecki.budget.passwordmanagement.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import pl.konradboniecki.budget.passwordmanagement.model.NewPasswordRequest;
import pl.konradboniecki.budget.passwordmanagement.service.AccountPasswordService;
import pl.konradboniecki.budget.passwordmanagement.service.NewPasswordRequestRepository;
import pl.konradboniecki.budget.passwordmanagement.service.NewPasswordRequestService;
import pl.konradboniecki.chassis.testtools.TestBase;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.FOUND;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = "spring.cloud.config.enabled=false"
)
public class ResetPasswdControllerChangePasswdTests extends TestBase {

    @Autowired
    private TestRestTemplate rest;
    @Autowired
    private AccountPasswordService accountPasswordService;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private NewPasswordRequestService newPasswordRequestService;
    @MockBean
    private NewPasswordRequestRepository newPasswordRequestRepository;
    @MockBean
    private RestTemplate restTemplate;
    @LocalServerPort
    private int port;
    private String baseUrl;
    @Value("${budget.baseUrl.accountManagement}")
    private String BASE_URL;

    private Long notExistingId;
    private Long existingId;
    private NewPasswordRequest newPasswordRequestEntity;
    private String validResetCode;
    private String invalidResetCode;
    private HttpEntity httpEntity;

    @BeforeAll
    public void setUp() {
        baseUrl = "http://localhost:" + port;
        notExistingId = 100L;
        existingId = 50L;
        validResetCode = "valid_reset_code";
        invalidResetCode = "invalid_reset_code";
        doNothing().when(newPasswordRequestRepository).deleteById(anyLong());
        doNothing().when(restTemplate).put(anyString(), any(ObjectNode.class));

        newPasswordRequestEntity = new NewPasswordRequest();
        newPasswordRequestEntity.setAccountId(existingId);
        newPasswordRequestEntity.setNewPassword("password");
        newPasswordRequestEntity.setApplyTime(ZonedDateTime.now());
        newPasswordRequestEntity.setResetCode(validResetCode);

        assertThat(rest.getForEntity(baseUrl + "/actuator/health", String.class).getStatusCodeValue()).isEqualTo(200);

        httpEntity = new HttpEntity<>(new HttpHeaders());
    }

    @Test
    @DisplayName("Log Error when invalid Id")
    public void givenNotExistingId_whenChangePassword_thenLogError() {
        // Given
        when(newPasswordRequestRepository.findById(notExistingId))
                .thenReturn(Optional.empty());
        String url = baseUrl + "/api/reset-password/" + notExistingId + "/" + invalidResetCode;
        // When
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, String.class);
        // Then
        String log = getLog();
        assertTrue(log.matches(".*New password not found for id: \\d*.*"));
    }

    @Test
    @DisplayName("Redirect when invalid Id")
    public void givenNotExistingId_whenChangePassword_thenRedirectedWith302() {
        // Given
        when(newPasswordRequestRepository.findById(notExistingId))
                .thenReturn(Optional.empty());
        String url = baseUrl + "/api/reset-password/" + notExistingId + "/" + invalidResetCode;
        // When
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, String.class);
        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(FOUND);
    }

    @Test
    @DisplayName("Log invalid reset code")
    public void givenExistingIdAndInvalidResetCode_whenChangePassword_thenLogError() {
        // Given
        when(newPasswordRequestRepository.findById(existingId))
                .thenReturn(Optional.of(newPasswordRequestEntity));
        String url = baseUrl + "/api/reset-password/" + existingId + "/" + invalidResetCode;
        // When
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, String.class);
        // Then
        String log = getLog();
        assertTrue(log.matches(".*Invalid resetCodeFromUrl: invalid_reset_code, for id: \\d*"));
    }

    @Test
    @DisplayName("Redirect to main page when invalid reset code")
    public void givenExistingIdAndInvalidResetCode_whenChangePassword_thenRedirectedWith302() {
        // Given
        when(newPasswordRequestRepository.findById(existingId))
                .thenReturn(Optional.of(newPasswordRequestEntity));
        String url = baseUrl + "/api/reset-password/" + existingId + "/" + invalidResetCode;
        // When
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, String.class);
        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(FOUND);
    }

    @Test
    @DisplayName("New password request is removed on valid params")
    public void givenExistingIdAndValidResetCode_whenChangePassword_thenNewPasswordRequestIsRemoved() {
        // Given
        when(newPasswordRequestRepository.findById(existingId))
                .thenReturn(Optional.of(newPasswordRequestEntity));
        String url = baseUrl + "/api/reset-password/" + existingId + "/" + validResetCode;
        // When
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, String.class);
        // Then
        verify(newPasswordRequestRepository, times(1)).deleteById(existingId);
    }

    @Test
    @DisplayName("Modify Password Request is sent to account service")
    @Disabled("TODO: use contract testing to support those tests. ")
    public void givenExistingIdAndValidResetCode_whenChangePassword_thenChangePasswordRequestIsSent() {
        // Given
        ArgumentCaptor<String> baseUrlArg = ArgumentCaptor.forClass(String.class);
        when(newPasswordRequestRepository.findById(existingId))
                .thenReturn(Optional.of(newPasswordRequestEntity));
//        doNothing().when(restTemplate).exchange(baseUrlArg.capture(),
//                eq(HttpMethod.PUT),
//                any(HttpEntity.class),
//                eq(String.class));
        String url = baseUrl + "/api/reset-password/" + existingId + "/" + validResetCode;

        // When
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, String.class);
        // Then
        assertThat(baseUrlArg.getValue()).isEqualTo(BASE_URL + "/api/account/change-password");
        verify(restTemplate, times(1)).put(anyString(), any(ObjectNode.class));
    }

    @Test
    @DisplayName("Logs are populated when changed password")
    public void givenExistingIdAndValidResetCode_whenChangePassword_thenLogsArePopulated() {
        // Given
        when(newPasswordRequestRepository.findById(existingId))
                .thenReturn(Optional.of(newPasswordRequestEntity));
        String url = baseUrl + "/api/reset-password/" + existingId + "/" + validResetCode;
        // When
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, String.class);
        // Then
        String log = getLog();
        Assertions.assertAll(
                () -> assertTrue(log.matches(".*Deleted NewPasswordRequest with id: \\d*.*")),
                () -> assertTrue(log.matches(".*Change password request sent for account with id: \\d*\\..*"))
        );
    }

    @Test
    @DisplayName("Redirect when changed password")
    public void givenExistingIdAndValidResetCode_whenChangePassword_thenRedirectedWith302() {
        // Given
        when(newPasswordRequestRepository.findById(existingId))
                .thenReturn(Optional.of(newPasswordRequestEntity));
        String url = baseUrl + "/api/reset-password/" + existingId + "/" + validResetCode;
        // When
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, String.class);
        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(FOUND);
    }
}
