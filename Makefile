NETWORK_NAME=rtree-network

DATABASE_HOST=Database
DATABASE_PORT=3306
DATABASE_USER=root
DATABASE_PASSWORD=password
DATASOURCE_DRIVER_CLASS_NAME=org.mariadb.jdbc.Driver
DB_IMAGE_NAME=kiralyzoltan98/mariadb-schema:latest

INSTANCE1_CONTAINER_NAME=Instance1
INSTANCE2_CONTAINER_NAME=Instance2
INSTANCE_IMAGE_NAME=kiralyzoltan98/rtree:latest

run: network database_start instance1_run instance2_run

network:
	-podman network exists $(NETWORK_NAME) || podman network create $(NETWORK_NAME)

instance1_start: instance1_run
	-podman start $(INSTANCE1_CONTAINER_NAME)

instance2_start: instance2_run
	-podman start $(INSTANCE2_CONTAINER_NAME)

instance1_run: gradle_build network
	-podman run --name $(INSTANCE1_CONTAINER_NAME) --network $(NETWORK_NAME) -e APPLICATION_INSTANCE_NAME=$(INSTANCE1_CONTAINER_NAME) -e DATABASE_HOST=jdbc:mariadb://$(DATABASE_HOST):$(DATABASE_PORT)/rtree -e DATABASE_USER=$(DATABASE_USER) -e DATABASE_PASSWORD=$(DATABASE_PASSWORD) -e DATASOURCE_DRIVER_CLASS_NAME=$(DATASOURCE_DRIVER_CLASS_NAME) -p 8081:8080 -d $(INSTANCE_IMAGE_NAME)

instance2_run: gradle_build network
	-podman run --name $(INSTANCE2_CONTAINER_NAME) --network $(NETWORK_NAME) -e APPLICATION_INSTANCE_NAME=$(INSTANCE2_CONTAINER_NAME) -e DATABASE_HOST=jdbc:mariadb://$(DATABASE_HOST):$(DATABASE_PORT)/rtree -e DATABASE_USER=$(DATABASE_USER) -e DATABASE_PASSWORD=$(DATABASE_PASSWORD) -e DATASOURCE_DRIVER_CLASS_NAME=$(DATASOURCE_DRIVER_CLASS_NAME) -p 8082:8080 -d $(INSTANCE_IMAGE_NAME)

database_start: database_run
	-podman start $(DATABASE_HOST)

database_run: gradle_build network
	-podman run --name $(DATABASE_HOST) --network $(NETWORK_NAME) -e MYSQL_ROOT_PASSWORD=$(DATABASE_PASSWORD) -p 3306:3306 -d $(DB_IMAGE_NAME)

gradle_build:
	podman run --rm -u root -v "$(CURDIR):/home/gradle/project" -w /home/gradle/project gradle:8.12.0-jdk21-corretto gradle build

database_build:
	build -f Dockerfile -t kiralyzoltan98/mariadb-schema:latest

instance_build:
	build -f Dockerfile -t kiralyzoltan98/rtree:latest