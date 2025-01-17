FROM mariadb:lts-ubi9

ENV MYSQL_ROOT_PASSWORD=password
ENV MYSQL_DATABASE=rtree

COPY schema.sql /docker-entrypoint-initdb.d/schema.sql

EXPOSE 3306