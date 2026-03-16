package com.finalbid.notification.service;

import com.finalbid.notification.dto.NotificationEvent;
import com.finalbid.notification.handler.NotificationEmail;
import com.finalbid.notification.handler.NotificationHandler;
import com.finalbid.notification.handler.NotificationHandlerFactory;
import com.finalbid.notification.model.NotificationLog;
import com.finalbid.notification.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationHandlerFactory factory;
    private final JavaMailSender mailSender;
    private final NotificationLogRepository logRepository;

    public NotificationServiceImpl(NotificationHandlerFactory factory,
                                   JavaMailSender mailSender,
                                   NotificationLogRepository logRepository) {
        this.factory = factory;
        this.mailSender = mailSender;
        this.logRepository = logRepository;
    }

    @Override
    @Transactional
    public void handle(NotificationEvent event) {
        if (alreadySent(event)) {
            log.info("Event already sent, skipping. eventType={} referenceId={}", event.eventType(), event.referenceId());
            return;
        }

        NotificationHandler handler = factory.getHandler(event.eventType());
        NotificationEmail email = handler.buildEmail(event);

        try {
            sendEmail(email);
            logSuccess(event, email.to());
        } catch (Exception e) {
            log.error("Failed to send email. eventType={} referenceId={} error={}", 
                      event.eventType(), event.referenceId(), e.getMessage());
            logFailure(event, email.to(), e.getMessage());
        }
    }

    private boolean alreadySent(NotificationEvent event) {
        return logRepository.existsByReferenceIdAndEventTypeAndStatus(
                event.referenceId(),
                event.eventType(),
                NotificationLog.NotificationStatus.SENT
        );
    }

    private void sendEmail(NotificationEmail email) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

        helper.setTo(email.to());
        helper.setSubject(email.subject());
        helper.setText(email.textBody(), email.htmlBody()); // textBody is fallback, htmlBody is main content

        // We use default From address configured in Spring properties (spring.mail.username or a defaults)
        // Usually we set 'from' if needed, e.g. helper.setFrom("noreply@finalbid.com");
        
        mailSender.send(mimeMessage);
    }

    private void logSuccess(NotificationEvent event, String recipient) {
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setEventType(event.eventType());
        notificationLog.setRecipientEmail(recipient);
        notificationLog.setReferenceId(event.referenceId());
        notificationLog.setStatus(NotificationLog.NotificationStatus.SENT);
        notificationLog.setSentAt(LocalDateTime.now());
        logRepository.save(notificationLog);
    }

    private void logFailure(NotificationEvent event, String recipient, String errorMessage) {
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setEventType(event.eventType());
        notificationLog.setRecipientEmail(recipient);
        notificationLog.setReferenceId(event.referenceId());
        notificationLog.setStatus(NotificationLog.NotificationStatus.FAILED);
        notificationLog.setSentAt(LocalDateTime.now());
        notificationLog.setErrorMessage(errorMessage);
        logRepository.save(notificationLog);
    }
}
