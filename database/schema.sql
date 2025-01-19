create table history
(
    id         bigint       not null auto_increment
        primary key,
    username   varchar(255) null,
    created_at datetime(6)  null,
    json_data  longtext     null
);
