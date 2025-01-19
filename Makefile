NETWORK_NAME=rtree-network

DATABASE_HOST=Database
DATABASE_PORT=3306
DATABASE_USER=root
DATABASE_PASSWORD=password
DATASOURCE_DRIVER_CLASS_NAME=org.mariadb.jdbc.Driver
DB_IMAGE_NAME=kiralyzoltan98/mariadb-schema:latest
TIMEZONE=Europe/Budapest

INSTANCE1_CONTAINER_NAME=Instance1
INSTANCE2_CONTAINER_NAME=Instance2
INSTANCE_IMAGE_NAME=kiralyzoltan98/rtree:latest

WAIT := $(shell sh -c "uname" 2>/dev/null || echo Windows)

ifeq ($(WAIT), Windows)
    WAIT_5 := powershell -Command "Start-Sleep -Seconds 5"
else
    WAIT_5 := sleep 5
endif

timeout:
	$(WAIT_5)

run: network database_start instance1_start instance2_start

network: # Create network if not exists
	podman network exists $(NETWORK_NAME) || podman network create $(NETWORK_NAME)

instance1_start: instance1_run timeout
	podman start $(INSTANCE1_CONTAINER_NAME)

instance2_start: instance2_run timeout
	podman start $(INSTANCE2_CONTAINER_NAME)

instance1_run: gradle_build network
	podman container exists $(INSTANCE1_CONTAINER_NAME) || podman run --name $(INSTANCE1_CONTAINER_NAME) --network $(NETWORK_NAME) -e APPLICATION_INSTANCE_NAME=$(INSTANCE1_CONTAINER_NAME) -e TZ=$(TIMEZONE) -e DATABASE_HOST=jdbc:mariadb://$(DATABASE_HOST):$(DATABASE_PORT)/rtree -e DATABASE_USER=$(DATABASE_USER) -e DATABASE_PASSWORD=$(DATABASE_PASSWORD) -e DATASOURCE_DRIVER_CLASS_NAME=$(DATASOURCE_DRIVER_CLASS_NAME) -p 8081:8080 -d $(INSTANCE_IMAGE_NAME)

instance2_run: gradle_build network
	podman container exists $(INSTANCE2_CONTAINER_NAME) || podman run --name $(INSTANCE2_CONTAINER_NAME) --network $(NETWORK_NAME) -e APPLICATION_INSTANCE_NAME=$(INSTANCE2_CONTAINER_NAME) -e TZ=$(TIMEZONE) -e DATABASE_HOST=jdbc:mariadb://$(DATABASE_HOST):$(DATABASE_PORT)/rtree -e DATABASE_USER=$(DATABASE_USER) -e DATABASE_PASSWORD=$(DATABASE_PASSWORD) -e DATASOURCE_DRIVER_CLASS_NAME=$(DATASOURCE_DRIVER_CLASS_NAME) -p 8082:8080 -d $(INSTANCE_IMAGE_NAME)

database_start: database_run
	podman start $(DATABASE_HOST)

database_run: gradle_build network
	podman container exists $(DATABASE_HOST) || podman run --name $(DATABASE_HOST) --network $(NETWORK_NAME) -e MYSQL_ROOT_PASSWORD=$(DATABASE_PASSWORD) -p 3306:3306 -d $(DB_IMAGE_NAME)

gradle_build: # Build project with gradle
	podman run --rm -u root -v "$(CURDIR):/home/gradle/project" -w /home/gradle/project gradle:8.12.0-jdk21-corretto gradle build

database_build:
	podman build -f database/Dockerfile -t kiralyzoltan98/mariadb-schema:latest

instance_build:
	podman build -f Dockerfile -t kiralyzoltan98/rtree:latest