CREATE TABLE taskify_users
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    user_name     VARCHAR(20)           NULL,
    user_password VARCHAR(255)           NULL,
    CONSTRAINT pk_taskify_users PRIMARY KEY (id)
);

create table user_roles (user_id bigint not null, roles varchar(255));