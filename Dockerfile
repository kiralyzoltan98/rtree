FROM gradle:8.12.0-jdk21-corretto as builder
COPY . /home/gradle/project

WORKDIR /home/gradle/project

RUN gradle build

FROM amazoncorretto:21-al2023-jdk

COPY --from=builder /home/gradle/project/build/libs/rtree-0.0.1-SNAPSHOT.jar /opt/rtree/app.jar

RUN yum install shadow-utils util-linux -y

COPY ./entrypoint /entrypoint

RUN chmod +x /entrypoint

CMD ["/entrypoint"]