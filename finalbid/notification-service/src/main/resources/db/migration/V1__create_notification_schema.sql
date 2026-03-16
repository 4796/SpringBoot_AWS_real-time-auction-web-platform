CREATE SCHEMA IF NOT EXISTS notification_schema;

CREATE TABLE notification_schema.notification_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    reference_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    error_message TEXT
);
