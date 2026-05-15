package io.kemalthes.semesterwork3.apigateway.dto;

@SuppressWarnings("unused")
public class AuthFilterConfig {

    private boolean required = true;

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
