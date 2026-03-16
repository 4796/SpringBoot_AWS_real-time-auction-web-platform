package com.finalbid.notification.service;

import com.finalbid.notification.dto.NotificationEvent;

public interface NotificationService {
    void handle(NotificationEvent event);
}
