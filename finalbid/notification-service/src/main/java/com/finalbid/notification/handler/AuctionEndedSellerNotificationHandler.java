package com.finalbid.notification.handler;

import com.finalbid.notification.dto.NotificationEvent;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class AuctionEndedSellerNotificationHandler implements NotificationHandler {
    private final TemplateEngine templateEngine;

    public AuctionEndedSellerNotificationHandler(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public NotificationEmail buildEmail(NotificationEvent event) {
        Context context = new Context();
        context.setVariable("sellerUsername", event.sellerUsername());
        context.setVariable("auctionTitle", event.auctionTitle());
        context.setVariable("finalPrice", event.finalPrice());
        context.setVariable("winnerUsername", event.winnerUsername());
        context.setVariable("winnerEmail", event.winnerEmail());

        String subject = "Your auction ended: " + event.auctionTitle();
        String htmlBody = templateEngine.process("email/seller-sold", context);
        
        return new NotificationEmail(
            event.recipientEmail(), // seller's email
            subject,
            htmlBody,
            subject + " sold for " + event.finalPrice()
        );
    }

    @Override
    public String getSupportedEventType() {
        return "AUCTION_ENDED_SELLER";
    }
}
