package com.example.oms.dto;

import com.example.oms.entity.NotificationType;

public class NotificationDispatchResultDto {

    private NotificationType type;
    private boolean success;
    private String message;

    public NotificationDispatchResultDto() {
    }

    public NotificationDispatchResultDto(NotificationType type, boolean success, String message) {
        this.type = type;
        this.success = success;
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

