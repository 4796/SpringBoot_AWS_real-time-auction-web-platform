package com.finalbid.notification.handler;

import com.finalbid.notification.dto.NotificationEvent;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class UserOutbidNotificationHandler implements NotificationHandler {
    private final TemplateEngine templateEngine;

    public UserOutbidNotificationHandler(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public NotificationEmail buildEmail(NotificationEvent event) {
        Context context = new Context();
        context.setVariable("username", event.username());
        context.setVariable("auctionTitle", event.auctionTitle());
        context.setVariable("currentPrice", event.currentPrice());
        context.setVariable("auctionLink", event.auctionLink());

        String subject = "You've been outbid on " + event.auctionTitle();
        String htmlBody = templateEngine.process("email/outbid", context);
        
        return new NotificationEmail(
            event.recipientEmail(),
            subject,
            htmlBody,
            subject + ". Current price is " + event.currentPrice()
        );
    }

    @Override
    public String getSupportedEventType() {
        return "BID_PLACED";
    }
}
