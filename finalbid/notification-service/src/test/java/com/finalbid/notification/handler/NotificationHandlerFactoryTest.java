package com.finalbid.notification.handler;

import com.finalbid.notification.exception.UnsupportedEventTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.thymeleaf.TemplateEngine;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class NotificationHandlerFactoryTest {

    private NotificationHandlerFactory factory;

    @BeforeEach
    void setUp() {
        TemplateEngine engine = Mockito.mock(TemplateEngine.class);
        List<NotificationHandler> handlers = List.of(
                new UserRegisteredNotificationHandler(engine),
                new UserOutbidNotificationHandler(engine),
                new AuctionEndedWinnerNotificationHandler(engine),
                new AuctionEndedSellerNotificationHandler(engine),
                new AuctionEndedNoBidsNotificationHandler(engine)
        );
        factory = new NotificationHandlerFactory(handlers);
    }

    @Test
    void testGetHandler_Success() {
        assertInstanceOf(UserRegisteredNotificationHandler.class, factory.getHandler("USER_REGISTERED"));
        assertInstanceOf(UserOutbidNotificationHandler.class, factory.getHandler("USER_OUTBID"));
        assertInstanceOf(AuctionEndedWinnerNotificationHandler.class, factory.getHandler("AUCTION_ENDED_WINNER"));
        assertInstanceOf(AuctionEndedSellerNotificationHandler.class, factory.getHandler("AUCTION_ENDED_SELLER"));
        assertInstanceOf(AuctionEndedNoBidsNotificationHandler.class, factory.getHandler("AUCTION_ENDED_NO_BIDS"));
    }

    @Test
    void testGetHandler_Unknown_ThrowsException() {
        assertThrows(UnsupportedEventTypeException.class, () -> factory.getHandler("UNKNOWN_EVENT"));
    }
}
