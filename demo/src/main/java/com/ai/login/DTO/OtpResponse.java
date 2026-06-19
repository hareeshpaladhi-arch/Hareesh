package com.ai.login.DTO;

public class OtpResponse {

    private boolean success;
    private String code;
    private String message;

    public OtpResponse(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
