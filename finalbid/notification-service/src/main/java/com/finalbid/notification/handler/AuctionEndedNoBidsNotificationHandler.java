package com.finalbid.notification.handler;

import com.finalbid.notification.dto.NotificationEvent;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class AuctionEndedNoBidsNotificationHandler implements NotificationHandler {
    private final TemplateEngine templateEngine;

    public AuctionEndedNoBidsNotificationHandler(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public NotificationEmail buildEmail(NotificationEvent event) {
        Context context = new Context();
        context.setVariable("sellerUsername", event.sellerUsername());
        context.setVariable("auctionTitle", event.auctionTitle());

        String subject = "Your auction ended with no bids: " + event.auctionTitle();
        String htmlBody = templateEngine.process("email/seller-no-bids", context);
        
        return new NotificationEmail(
            event.recipientEmail(), // seller's email
            subject,
            htmlBody,
            subject
        );
    }

    @Override
    public String getSupportedEventType() {
        return "AUCTION_ENDED_NO_BIDS";
    }
}
