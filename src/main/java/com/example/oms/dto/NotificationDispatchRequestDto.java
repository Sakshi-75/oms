package com.example.oms.dto;

public class NotificationDispatchRequestDto {

    private boolean emailFail;
    private boolean smsFail;
    private boolean pushFail;

    private boolean emailThrow;
    private boolean smsThrow;
    private boolean pushThrow;

    private int delayMillis;

    public NotificationDispatchRequestDto() {
    }

    public boolean isEmailFail() {
        return emailFail;
    }

    public void setEmailFail(boolean emailFail) {
        this.emailFail = emailFail;
    }

    public boolean isSmsFail() {
        return smsFail;
    }

    public void setSmsFail(boolean smsFail) {
        this.smsFail = smsFail;
    }

    public boolean isPushFail() {
        return pushFail;
    }

    public void setPushFail(boolean pushFail) {
        this.pushFail = pushFail;
    }

    public boolean isEmailThrow() {
        return emailThrow;
    }

    public void setEmailThrow(boolean emailThrow) {
        this.emailThrow = emailThrow;
    }

    public boolean isSmsThrow() {
        return smsThrow;
    }

    public void setSmsThrow(boolean smsThrow) {
        this.smsThrow = smsThrow;
    }

    public boolean isPushThrow() {
        return pushThrow;
    }

    public void setPushThrow(boolean pushThrow) {
        this.pushThrow = pushThrow;
    }

    public int getDelayMillis() {
        return delayMillis;
    }

    public void setDelayMillis(int delayMillis) {
        this.delayMillis = delayMillis;
    }
}

