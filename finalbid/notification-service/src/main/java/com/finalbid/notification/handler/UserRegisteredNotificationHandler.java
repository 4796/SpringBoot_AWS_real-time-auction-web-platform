package com.finalbid.notification.handler;

import com.finalbid.notification.dto.NotificationEvent;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class UserRegisteredNotificationHandler implements NotificationHandler {
    private final TemplateEngine templateEngine;

    public UserRegisteredNotificationHandler(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public NotificationEmail buildEmail(NotificationEvent event) {
        Context context = new Context();
        context.setVariable("username", event.username());
        context.setVariable("verificationLink", event.verificationLink());

        String htmlBody = templateEngine.process("email/verification", context);
        
        return new NotificationEmail(
            event.recipientEmail(),
            "Verify your FinalBid account",
            htmlBody,
            "Please verify your account using this link: " + event.verificationLink()
        );
    }

    @Override
    public String getSupportedEventType() {
        return "USER_REGISTERED";
    }
}
