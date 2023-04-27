create table revinfo
(
    rev      integer not null auto_increment,
    revtstmp bigint,
    primary key (rev)
) engine = InnoDB;
create table comments_aud
(
    id           bigint  not null,
    rev          integer not null,
    revtype      tinyint,
    comment_date datetime(6),
    poster_name  varchar(255),
    value        varchar(255),
    ticket_id    bigint,
    primary key (rev, id)
) engine = InnoDB;
create table tickets_aud
(
    id                 bigint  not null,
    rev                integer not null,
    revtype            tinyint,
    ticket_description varchar(250),
    posted_at          datetime(6),
    priority           varchar(255),
    status             varchar(255),
    ticket_title       varchar(75),
    ticket_type        varchar(255),
    assignee_id        bigint,
    poster_id          bigint,
    primary key (rev, id)
) engine = InnoDB;
alter table if exists comments_aud add constraint FKprr41p1i93lyk0qha4ab9lfdb foreign key (rev) references revinfo (rev);
alter table if exists tickets_aud add constraint FK1tdd5eyb1d15825plba3jmw8h foreign key (rev) references revinfo (rev);

