package com.sunrisedental.strategy;

import java.util.HashMap;
import java.util.Map;

public class NotificationStrategyFactory {

    private static final Map<String, NotificationStrategy> STRATEGIES = new HashMap<>();

    static {
        STRATEGIES.put("email", new EmailNotificationStrategy());
        STRATEGIES.put("sms", new SmsNotificationStrategy());
    }

    private NotificationStrategyFactory() {
    }

    public static NotificationStrategy getStrategy(String type) {
        NotificationStrategy strategy = STRATEGIES.get(type == null ? "" : type.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported notification type: " + type);
        }
        return strategy;
    }
}
