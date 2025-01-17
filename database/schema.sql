create table history
(
    id         bigint       not null
        primary key,
    user       varchar(255) null,
    created_at datetime(6)  null,
    json_data  text         null
);
