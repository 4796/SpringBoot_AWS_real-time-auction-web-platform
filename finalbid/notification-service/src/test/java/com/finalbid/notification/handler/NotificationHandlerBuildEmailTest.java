package com.finalbid.notification.handler;

import com.finalbid.notification.dto.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class NotificationHandlerBuildEmailTest {

    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        templateEngine = Mockito.mock(TemplateEngine.class);
        when(templateEngine.process(any(String.class), any(Context.class)))
                .thenReturn("<html>mocked HTML</html>");
    }

    @Test
    void testUserRegisteredEmail() {
        var handler = new UserRegisteredNotificationHandler(templateEngine);
        var event = new NotificationEvent(
                "USER_REGISTERED", "user@test.com", UUID.randomUUID(),
                "testuser", null, null, null, "http://verify", null, null, null, null, null
        );

        NotificationEmail email = handler.buildEmail(event);
        assertEquals("user@test.com", email.to());
        assertEquals("Verify your FinalBid account", email.subject());
        assertEquals("<html>mocked HTML</html>", email.htmlBody());
    }

    @Test
    void testAuctionEndedWinnerEmail() {
        var handler = new AuctionEndedWinnerNotificationHandler(templateEngine);
        var event = new NotificationEvent(
                "AUCTION_ENDED_WINNER", "winner@test.com", UUID.randomUUID(),
                null, "Vintage Camera", null, null, null, new BigDecimal("150.00"),
                "seller@test.com", null, null, "winnerUser"
        );

        NotificationEmail email = handler.buildEmail(event);
        assertEquals("winner@test.com", email.to());
        assertEquals("You won: Vintage Camera", email.subject());
    }
}
