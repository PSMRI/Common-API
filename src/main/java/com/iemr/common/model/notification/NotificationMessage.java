package com.iemr.common.model.notification;

import lombok.Data;

import java.util.Map;

@Data
public class NotificationMessage {
    private String appType;
    private String topic;
    private String title;
    private String body;
    private Map<String ,String> data;
}
