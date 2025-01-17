DB_CONTAINER_NAME=Database
DB_IMAGE_NAME=kiralyzoltan98/mariadb-schema:latest

.PHONY: run database_start

run: database_start

database_start: database_run
	-podman start Database

database_run:
	-podman run --name $(DB_CONTAINER_NAME) -e MYSQL_ROOT_PASSWORD=password -p 3306:3306 -d $(DB_IMAGE_NAME)
