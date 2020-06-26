package pl.konradboniecki.budget.passwordmanagement.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import pl.konradboniecki.budget.passwordmanagement.model.NewPasswordForm;
import pl.konradboniecki.budget.passwordmanagement.model.ViewTemplate;

@Slf4j
@Controller
@RequestMapping("/api/reset-password")
public class FormController {

    @GetMapping(value = "/form")
    public ModelAndView showLostPasswordForm() {
        return new ModelAndView(ViewTemplate.LOST_PASSWORD_FORM.getViewName(), ViewTemplate.LOST_PASSWORD_FORM.getModelName(), new NewPasswordForm());
    }
}
