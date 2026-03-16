package com.finalbid.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MailConfig {
    // Relying on application properties for JavaMailSender auto-configuration:
    // spring.mail.host, spring.mail.port, spring.mail.username, spring.mail.password

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
