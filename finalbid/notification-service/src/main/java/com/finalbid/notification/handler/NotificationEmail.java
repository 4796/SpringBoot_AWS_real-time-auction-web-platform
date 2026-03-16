package com.finalbid.notification.handler;

public record NotificationEmail(
    String to,
    String subject,
    String htmlBody,
    String textBody
) {}
