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

OS := $(shell sh -c "uname" 2>/dev/null || echo Windows)

# Set OS-specific commands
ifeq ($(OS), Windows)
    NULL := NUL
    WAIT_5 := powershell -Command "Start-Sleep -Seconds 5"
else
    NULL := /dev/null
    WAIT_5 := sleep 5
endif

timeout:
	$(WAIT_5)

DOCKER := $(shell podman -v > $(NULL) 2>&1 && echo podman || (docker -v > $(NULL) 2>&1 && echo docker || echo none))

ifeq ($(DOCKER), none)
$(error Neither podman nor docker is installed on this system)
endif

run: start_docker_engine network database_start instance1_start instance2_start

start_docker_engine:
	-$(DOCKER) ps || $(DOCKER) machine start

network: # Create network if not exists
	$(DOCKER) network exists $(NETWORK_NAME) || $(DOCKER) network create $(NETWORK_NAME)

instance1_start: instance1_run timeout
	$(DOCKER) start $(INSTANCE1_CONTAINER_NAME)

instance2_start: instance2_run timeout
	$(DOCKER) start $(INSTANCE2_CONTAINER_NAME)

instance1_run: gradle_build network
	$(DOCKER) container exists $(INSTANCE1_CONTAINER_NAME) || $(DOCKER) run --name $(INSTANCE1_CONTAINER_NAME) --network $(NETWORK_NAME) -e APPLICATION_INSTANCE_NAME=$(INSTANCE1_CONTAINER_NAME) -e TZ=$(TIMEZONE) -e DATABASE_HOST=jdbc:mariadb://$(DATABASE_HOST):$(DATABASE_PORT)/rtree -e DATABASE_USER=$(DATABASE_USER) -e DATABASE_PASSWORD=$(DATABASE_PASSWORD) -e DATASOURCE_DRIVER_CLASS_NAME=$(DATASOURCE_DRIVER_CLASS_NAME) -p 8081:8080 -d $(INSTANCE_IMAGE_NAME)

instance2_run: gradle_build network
	$(DOCKER) container exists $(INSTANCE2_CONTAINER_NAME) || $(DOCKER) run --name $(INSTANCE2_CONTAINER_NAME) --network $(NETWORK_NAME) -e APPLICATION_INSTANCE_NAME=$(INSTANCE2_CONTAINER_NAME) -e TZ=$(TIMEZONE) -e DATABASE_HOST=jdbc:mariadb://$(DATABASE_HOST):$(DATABASE_PORT)/rtree -e DATABASE_USER=$(DATABASE_USER) -e DATABASE_PASSWORD=$(DATABASE_PASSWORD) -e DATASOURCE_DRIVER_CLASS_NAME=$(DATASOURCE_DRIVER_CLASS_NAME) -p 8082:8080 -d $(INSTANCE_IMAGE_NAME)

database_start: database_run
	$(DOCKER) start $(DATABASE_HOST)

database_run: gradle_build network
	$(DOCKER) container exists $(DATABASE_HOST) || $(DOCKER) run --name $(DATABASE_HOST) --network $(NETWORK_NAME) -e MYSQL_ROOT_PASSWORD=$(DATABASE_PASSWORD) -p 3306:3306 -d $(DB_IMAGE_NAME)

gradle_build: # Build project with gradle
	$(DOCKER) run --rm -u root -v "$(CURDIR):/home/gradle/project" -w /home/gradle/project gradle:8.12.0-jdk21-corretto gradle build

database_build:
	$(DOCKER) build -f database/Dockerfile -t kiralyzoltan98/mariadb-schema:latest

instance_build: gradle_build
	$(DOCKER) build -f Dockerfile -t kiralyzoltan98/rtree:latest