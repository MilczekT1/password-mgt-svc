package pl.konradboniecki.budget.passwordmanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.passwordmanagement.model.NewPasswordRequest;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.Optional;

@Slf4j
@Data
@Service
public class AccountPasswordService {

    private NewPasswordRequestService newPasswordRequestService;
    private RestTemplate restTemplate;
    private String changePasswordUrl;

    @Autowired
    public AccountPasswordService(NewPasswordRequestService newPasswordRequestService, RestTemplate restTemplate, @Value("${budget.baseUrl.accountManagement}") String baseUrl) {
        setNewPasswordRequestService(newPasswordRequestService);
        setRestTemplate(restTemplate);
        setChangePasswordUrl(baseUrl + "/api/account/change-password");
    }

    public void changePassword(String resetCodeFromUrl, Long id) {
        Optional<NewPasswordRequest> newPasswordOpt = newPasswordRequestService.findNewPasswordRequestById(id);
        if (newPasswordOpt.isPresent()) {
            String correctResetCode = newPasswordOpt.get().getResetCode();
            if (resetCodeFromUrl.equals(correctResetCode)) {
                String newPassword = newPasswordOpt.get().getNewPassword();
                sendRequestToChangePasswordInAccount(newPassword, id);
                newPasswordRequestService.deleteNewPasswordRequestById(id);
                log.info("Deleted NewPasswordRequest with id: " + id);
                return;
            }
            log.error("Invalid resetCodeFromUrl: " + resetCodeFromUrl + ", for id: " + id);
        } else {
            log.error("New password not found for id: " + id);
        }
    }

    private void sendRequestToChangePasswordInAccount(String newPassword, Long id) {
        ObjectNode json = new ObjectMapper().createObjectNode();
        json.put("AccountId", id);
        json.put("NewPassword", newPassword);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        // TODO: update to NewPasswordRequest also in account
        restTemplate.exchange(changePasswordUrl, HttpMethod.PUT, new HttpEntity<>(json, httpHeaders), String.class);
        log.info("Change password request sent for account with id: " + id + ".");
    }
}
