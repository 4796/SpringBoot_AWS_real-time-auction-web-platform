package com.finalbid.auction.messaging;

import java.util.Map;

public interface EventPublisher {
    void publish(String topic, String eventType, Map<String, Object> data);
}
