CREATE TABLE tickets
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    ticket_title              VARCHAR(255)          NULL,
    ticket_description VARCHAR(255)          NULL,
    posted_at           datetime              NULL,
    poster_id            BIGINT                NULL,
    assignee_id            BIGINT                NULL,
    priority           VARCHAR(255)          NULL,
    ticket_type        VARCHAR(255)          NULL,
    status             VARCHAR(255)          NULL,
    CONSTRAINT pk_tickets PRIMARY KEY (id)
);

ALTER TABLE tickets
    ADD CONSTRAINT FK_POSTER_ON_USER FOREIGN KEY (poster_id) REFERENCES taskify_users (id);
ALTER TABLE tickets
    ADD CONSTRAINT FK_ASSIGNEE_ON_USER FOREIGN KEY (assignee_id) REFERENCES taskify_users (id);
