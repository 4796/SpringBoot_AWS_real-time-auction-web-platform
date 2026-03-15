package com.finalbid.auction.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class BidWebSocketListener {

    private static final Logger logger = LoggerFactory.getLogger(BidWebSocketListener.class);
    private final SimpMessagingTemplate messagingTemplate;

    public BidWebSocketListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void handleBidMessage(String message, String channel) {
        logger.debug("Received Redis bid message on channel {}: {}", channel, message);
        // Channel is finalbid:ws:bid:{auctionId}
        String auctionId = channel.substring(channel.lastIndexOf(":") + 1);
        messagingTemplate.convertAndSend("/topic/auction/" + auctionId, message);
    }

    public void handleEndedMessage(String message, String channel) {
        logger.debug("Received Redis ended message on channel {}: {}", channel, message);
        // Channel is finalbid:ws:ended:{auctionId}
        String auctionId = channel.substring(channel.lastIndexOf(":") + 1);
        messagingTemplate.convertAndSend("/topic/auction/" + auctionId, message);
    }
}
