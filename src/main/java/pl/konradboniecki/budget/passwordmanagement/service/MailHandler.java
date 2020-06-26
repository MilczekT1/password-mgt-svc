package pl.konradboniecki.budget.passwordmanagement.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import pl.konradboniecki.budget.passwordmanagement.model.json.ActivationLinkRequest;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import static pl.konradboniecki.chassis.tools.RestTools.defaultPostHTTPHeaders;

@Data
@Slf4j
@Service
public class MailHandler {

    private RestTemplate restTemplate;
    private String resetPasswordMailUrl;

    @Autowired
    public MailHandler(RestTemplate restTemplate, @Value("${budget.baseUrl.mail}") String baseUrl) {
        setRestTemplate(restTemplate);
        setResetPasswordMailUrl(baseUrl + "/api/mail/reset-password");
    }

    public void requestMailWithResetCode(ActivationLinkRequest activationLinkRequest) {
        ObjectNode json = activationLinkRequest.prepareJsonForMail();
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        try {
            restTemplate.exchange(resetPasswordMailUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(json, headers),
                    String.class);
        } catch (HttpClientErrorException e) {
            String email = activationLinkRequest.getAccount().getEmail();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Email has not been set for " + email, e);
        }

        log.info("Mail request has been sent for " + activationLinkRequest.getAccount().getEmail());
    }
}
