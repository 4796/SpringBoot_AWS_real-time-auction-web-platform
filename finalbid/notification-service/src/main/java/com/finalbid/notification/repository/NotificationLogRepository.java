package com.finalbid.notification.repository;

import com.finalbid.notification.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {
    boolean existsByReferenceIdAndEventTypeAndStatus(UUID referenceId, String eventType, NotificationLog.NotificationStatus status);
}
