package com.finalbid.notification.service;

import com.finalbid.notification.dto.NotificationEvent;
import com.finalbid.notification.handler.NotificationEmail;
import com.finalbid.notification.handler.NotificationHandler;
import com.finalbid.notification.handler.NotificationHandlerFactory;
import com.finalbid.notification.model.NotificationLog;
import com.finalbid.notification.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    private NotificationHandlerFactory factory;
    private JavaMailSender mailSender;
    private NotificationLogRepository logRepository;
    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        factory = Mockito.mock(NotificationHandlerFactory.class);
        mailSender = Mockito.mock(JavaMailSender.class);
        logRepository = Mockito.mock(NotificationLogRepository.class);
        service = new NotificationServiceImpl(factory, mailSender, logRepository);
        
        MimeMessage mimeMessage = Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void testHandle_Idempotency_SkipsAlreadySent() {
        UUID refId = UUID.randomUUID();
        NotificationEvent event = new NotificationEvent("USER_REGISTERED", "test@test.com", refId, null, null, null, null, null, null, null, null, null, null);
        
        when(logRepository.existsByReferenceIdAndEventTypeAndStatus(refId, "USER_REGISTERED", NotificationLog.NotificationStatus.SENT))
            .thenReturn(true);

        service.handle(event);

        verify(factory, never()).getHandler(anyString());
        verify(mailSender, never()).send((MimeMessage) any());
    }

    @Test
    void testHandle_Success_SendsAndLogs() throws Exception {
        UUID refId = UUID.randomUUID();
        NotificationEvent event = new NotificationEvent("USER_REGISTERED", "test@test.com", refId, null, null, null, null, null, null, null, null, null, null);
        
        when(logRepository.existsByReferenceIdAndEventTypeAndStatus(refId, "USER_REGISTERED", NotificationLog.NotificationStatus.SENT))
            .thenReturn(false);

        NotificationHandler mockHandler = Mockito.mock(NotificationHandler.class);
        when(factory.getHandler("USER_REGISTERED")).thenReturn(mockHandler);
        when(mockHandler.buildEmail(event)).thenReturn(new NotificationEmail("test@test.com", "Subject", "html", "text"));

        service.handle(event);

        verify(mailSender, times(1)).send((MimeMessage) any());
        
        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(logRepository, times(1)).save(captor.capture());
        
        NotificationLog savedLog = captor.getValue();
        assertEquals("USER_REGISTERED", savedLog.getEventType());
        assertEquals("test@test.com", savedLog.getRecipientEmail());
        assertEquals(NotificationLog.NotificationStatus.SENT, savedLog.getStatus());
    }

    @Test
    void testHandle_Failure_LogsError() throws Exception {
        UUID refId = UUID.randomUUID();
        NotificationEvent event = new NotificationEvent("USER_REGISTERED", "test@test.com", refId, null, null, null, null, null, null, null, null, null, null);
        
        when(logRepository.existsByReferenceIdAndEventTypeAndStatus(refId, "USER_REGISTERED", NotificationLog.NotificationStatus.SENT))
            .thenReturn(false);

        NotificationHandler mockHandler = Mockito.mock(NotificationHandler.class);
        when(factory.getHandler("USER_REGISTERED")).thenReturn(mockHandler);
        when(mockHandler.buildEmail(event)).thenReturn(new NotificationEmail("test@test.com", "Subject", "html", "text"));

        doThrow(new RuntimeException("SMTP Server Down")).when(mailSender).send((MimeMessage) any());

        service.handle(event);
        
        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(logRepository, times(1)).save(captor.capture());
        
        NotificationLog savedLog = captor.getValue();
        assertEquals(NotificationLog.NotificationStatus.FAILED, savedLog.getStatus());
        assertNotNull(savedLog.getErrorMessage());
        assertEquals("SMTP Server Down", savedLog.getErrorMessage());
    }
}
