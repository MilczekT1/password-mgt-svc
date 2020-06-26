package pl.konradboniecki.budget.passwordmanagement.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = "spring.cloud.config.enabled=false"
)
public class FormControllerTest {

    @Autowired
    private TestRestTemplate rest;
    @LocalServerPort
    private int port;
    private String baseUrl;

    @BeforeAll
    public void beforeAll() {
        baseUrl = "http://localhost:" + port;
        assertThat(rest.getForEntity(baseUrl + "/actuator/health", String.class).getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("GET /api/reset-password/form uses valid template")
    public void givenHealthyApp_whenRequestForm_thenReturnNewPasswordFormView() {
        // Given:
        String url = baseUrl + "/api/reset-password/form";
        // When:
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()), String.class);
        // Then:
        String expectedBody = "<title>Lost Password</title>";
        assertThat(responseEntity.getBody()).contains(expectedBody);
    }
}
