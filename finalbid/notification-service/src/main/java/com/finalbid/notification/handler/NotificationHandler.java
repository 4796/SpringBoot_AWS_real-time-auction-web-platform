package com.finalbid.notification.handler;

import com.finalbid.notification.dto.NotificationEvent;

public interface NotificationHandler {
    NotificationEmail buildEmail(NotificationEvent event);
    String getSupportedEventType();
}
