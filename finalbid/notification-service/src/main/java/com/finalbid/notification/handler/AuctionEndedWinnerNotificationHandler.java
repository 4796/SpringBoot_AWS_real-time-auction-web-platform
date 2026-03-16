package com.finalbid.notification.handler;

import com.finalbid.notification.dto.NotificationEvent;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class AuctionEndedWinnerNotificationHandler implements NotificationHandler {
    private final TemplateEngine templateEngine;

    public AuctionEndedWinnerNotificationHandler(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public NotificationEmail buildEmail(NotificationEvent event) {
        Context context = new Context();
        context.setVariable("winnerUsername", event.winnerUsername());
        context.setVariable("auctionTitle", event.auctionTitle());
        context.setVariable("finalPrice", event.finalPrice());
        context.setVariable("sellerEmail", event.sellerEmail());

        String subject = "You won: " + event.auctionTitle();
        String htmlBody = templateEngine.process("email/winner", context);
        
        return new NotificationEmail(
            event.recipientEmail(), // winner's email
            subject,
            htmlBody,
            "Congratulations! " + subject + " for " + event.finalPrice()
        );
    }

    @Override
    public String getSupportedEventType() {
        return "AUCTION_ENDED_WINNER";
    }
}
