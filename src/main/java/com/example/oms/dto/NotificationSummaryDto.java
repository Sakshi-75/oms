package com.example.oms.dto;

public class NotificationSummaryDto {

    private long emailCount;
    private long smsCount;
    private long pushCount;

    private long successCount;
    private long failureCount;

    public NotificationSummaryDto() {
    }

    public long getEmailCount() {
        return emailCount;
    }

    public void setEmailCount(long emailCount) {
        this.emailCount = emailCount;
    }

    public long getSmsCount() {
        return smsCount;
    }

    public void setSmsCount(long smsCount) {
        this.smsCount = smsCount;
    }

    public long getPushCount() {
        return pushCount;
    }

    public void setPushCount(long pushCount) {
        this.pushCount = pushCount;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(long failureCount) {
        this.failureCount = failureCount;
    }
}

