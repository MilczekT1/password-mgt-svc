package pl.konradboniecki.budget.passwordmanagement.controller;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import pl.konradboniecki.budget.passwordmanagement.model.NewPasswordForm;
import pl.konradboniecki.budget.passwordmanagement.service.AccountFacade;
import pl.konradboniecki.budget.passwordmanagement.service.FormHandler;
import pl.konradboniecki.budget.passwordmanagement.service.NewPasswordRequestRepository;
import pl.konradboniecki.budget.passwordmanagement.service.NewPasswordRequestService;

import javax.validation.Valid;

import static pl.konradboniecki.budget.passwordmanagement.model.ViewTemplate.LOST_PASSWORD_FORM;

@Setter(AccessLevel.PRIVATE)
@Slf4j
@RestController
@RequestMapping("/api/reset-password")
public class ResetPasswdController {

    @Value("${budget.baseUrl.gateway}")
    private String BASE_URL;
    private NewPasswordRequestRepository newPasswordRequestRepository;
    private RestTemplate restTemplate;
    private AccountFacade accountFacade;
    private NewPasswordRequestService newPasswordRequestService;
    private FormHandler formHandler;

    @Autowired
    public ResetPasswdController(NewPasswordRequestRepository newPasswordRequestRepository, RestTemplate restTemplate, AccountFacade accountFacade, NewPasswordRequestService newPasswordRequestService, FormHandler formHandler) {
        setNewPasswordRequestRepository(newPasswordRequestRepository);
        setRestTemplate(restTemplate);
        setAccountFacade(accountFacade);
        setNewPasswordRequestService(newPasswordRequestService);
        setFormHandler(formHandler);
    }

    @GetMapping(value = "/{id}/{resetCode}")
    public ModelAndView changePassword(@PathVariable("id") Long id,
                                       @PathVariable("resetCode") String resetCodeFromUrl) {
        accountFacade.changePassword(resetCodeFromUrl, id);
        return new ModelAndView("redirect:" + BASE_URL);
    }

    @PostMapping(value = "/request")
    public ModelAndView processForm(@ModelAttribute("newPasswordForm")
                                    @Valid NewPasswordForm newPasswordForm,
                                    BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors())
            return new ModelAndView(LOST_PASSWORD_FORM.getViewName());
        else if (!newPasswordForm.isRepeatedPasswordTheSame())
            return new ModelAndView(LOST_PASSWORD_FORM.getViewName(), "repeatedPasswordFailure", true);

        formHandler.handleForm(newPasswordForm);

        return new ModelAndView("redirect:" + BASE_URL);
    }
}
