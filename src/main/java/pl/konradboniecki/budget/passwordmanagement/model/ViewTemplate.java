package pl.konradboniecki.budget.passwordmanagement.model;

import lombok.Getter;

@Getter
public enum ViewTemplate {
    LOST_PASSWORD_FORM("lostPasswordForm", "newPasswordForm");

    ViewTemplate(String viewName, String modelName) {
        this.viewName = viewName;
        this.modelName = modelName;
    }

    private String viewName;
    private String modelName;
}
