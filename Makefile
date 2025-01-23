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

INSTANCE_RUN_COMMON=--network $(NETWORK_NAME) -e USER=${USER} -e TZ=$(TIMEZONE) -e DATABASE_HOST=jdbc:mariadb://$(DATABASE_HOST):$(DATABASE_PORT)/rtree -e DATABASE_USER=$(DATABASE_USER) -e DATABASE_PASSWORD=$(DATABASE_PASSWORD) -e DATASOURCE_DRIVER_CLASS_NAME=$(DATASOURCE_DRIVER_CLASS_NAME) -d $(INSTANCE_IMAGE_NAME)

OS := $(shell sh -c "uname" 2>/dev/null || echo Windows)

# Set OS-specific commands
ifeq ($(OS), Windows)
    WAIT_5 := powershell -Command "Start-Sleep -Seconds 5"
else
    WAIT_5 := sleep 5
endif

timeout:
	$(WAIT_5)

run: start_docker_engine create_network database_start
    @make instance_run INSTANCE_PORT=8081
    @make instance_run INSTANCE_PORT=8082

start_docker_engine:
	-podman ps || podman machine start

create_network: # Create network if not exists
	podman network exists $(NETWORK_NAME) || podman network create $(NETWORK_NAME)

# Every environment variable that make sees when it starts up is transformed into a make variable with the same name and value.
instance_run: instance_build create_network
	podman run --rm -p $(INSTANCE_PORT):8080 $(INSTANCE_RUN_COMMON)

database_start: database_run
	podman start $(DATABASE_HOST)

database_run: create_network
	podman container exists $(DATABASE_HOST) || podman run --name $(DATABASE_HOST) --network $(NETWORK_NAME) -e MYSQL_ROOT_PASSWORD=$(DATABASE_PASSWORD) -p 3306:3306 -d $(DB_IMAGE_NAME)

database_build:
	podman build -f database/Dockerfile -t kiralyzoltan98/mariadb-schema:latest

instance_build:
	podman build -f Dockerfile -t kiralyzoltan98/rtree:latest

javadoc:
	podman run --rm -u root -v "$(CURDIR):/home/gradle/project" -w /home/gradle/project gradle:8.12.0-jdk21-corretto gradle javadoc

demo:
	echo ${USER}