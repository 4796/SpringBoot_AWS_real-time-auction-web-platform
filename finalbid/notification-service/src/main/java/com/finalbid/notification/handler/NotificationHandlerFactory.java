package com.finalbid.notification.handler;

import com.finalbid.notification.exception.UnsupportedEventTypeException;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationHandlerFactory {
    private final Map<String, NotificationHandler> handlers;

    public NotificationHandlerFactory(List<NotificationHandler> handlerList) {
        this.handlers = handlerList.stream()
            .collect(Collectors.toMap(
                NotificationHandler::getSupportedEventType,
                Function.identity()
            ));
    }

    public NotificationHandler getHandler(String eventType) {
        NotificationHandler handler = handlers.get(eventType);
        if (handler == null) {
            throw new UnsupportedEventTypeException(eventType);
        }
        return handler;
    }
}
