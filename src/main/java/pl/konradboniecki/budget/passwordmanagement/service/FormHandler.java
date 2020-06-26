package pl.konradboniecki.budget.passwordmanagement.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.passwordmanagement.model.NewPasswordForm;
import pl.konradboniecki.budget.passwordmanagement.model.NewPasswordRequest;
import pl.konradboniecki.budget.passwordmanagement.model.json.Account;
import pl.konradboniecki.budget.passwordmanagement.model.json.ActivationLinkRequest;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Data
@Slf4j
@Service
public class FormHandler {

    private RestTemplate restTemplate;
    private NewPasswordRequestService newPasswordRequestService;
    private MailHandler mailHandler;
    private AccountFacade accountFacade;
    private ResponseEntity<String> responseWithAccount;

    @Autowired
    public FormHandler(RestTemplate restTemplate, NewPasswordRequestService newPasswordRequestService, MailHandler mailHandler, AccountFacade accountFacade) {
        setRestTemplate(restTemplate);
        setNewPasswordRequestService(newPasswordRequestService);
        setMailHandler(mailHandler);
        setAccountFacade(accountFacade);
    }

    public void handleForm(NewPasswordForm newPasswordForm) throws Exception {
        log.info("Start handling form for " + newPasswordForm.getEmail());

        findAccountWithEmail(newPasswordForm.getEmail());

        ActivationLinkRequest activationLinkRequest = createActivationLinkRequestFromBody();
        activationLinkRequest.setResetCode(UUID.randomUUID().toString());

        saveNewPasswdRequestToDb(newPasswordForm, activationLinkRequest);
        mailHandler.requestMailWithResetCode(activationLinkRequest);

        log.info("End handling form for " + newPasswordForm.getEmail() + " with success.");
    }

    private void findAccountWithEmail(String email) {
        setResponseWithAccount(accountFacade.getAccountByEmail(email));
        log.info("Sent request for account " + email);
    }

    private ActivationLinkRequest createActivationLinkRequestFromBody() throws IOException {
        Map<String, Object> map = new ObjectMapper()
                .readValue(responseWithAccount.getBody(), new TypeReference<Map<String, Object>>() {
                });
        Account account = new Account()
                .setEmail(map.get("email").toString())
                .setFirstName(map.get("firstName").toString())
                .setLastName(map.get("lastName").toString())
                .setId(Long.valueOf(map.get("id").toString()));
        return new ActivationLinkRequest().setAccount(account);
    }

    private void saveNewPasswdRequestToDb(NewPasswordForm newPasswordForm, ActivationLinkRequest activationLinkRequest) {
        Long id = activationLinkRequest.getAccount().getId();
        NewPasswordRequest newPasswordRequest = new NewPasswordRequest(newPasswordForm, id, activationLinkRequest.getResetCode());

        newPasswordRequestService.saveNewPasswordRequest(newPasswordRequest);
        log.info("NewPasswordRequest has been saved for " + newPasswordForm.getEmail());
    }
}
