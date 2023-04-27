CREATE TABLE comments
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    value       TEXT          NOT NULL,
    poster_name  VARCHAR(20)         NOT NULL,
    comment_date datetime             NOT NULL,
    ticket_id   BIGINT                NOT NULL,
    CONSTRAINT pk_comment PRIMARY KEY (id)
);

ALTER TABLE comments
    ADD CONSTRAINT FK_COMMENT_ON_TICKET FOREIGN KEY (ticket_id) REFERENCES tickets (id);