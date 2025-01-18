FROM amazoncorretto:21-al2023-jdk

COPY ./build/libs/rtree-0.0.1-SNAPSHOT.jar /opt/rtree/app.jar

CMD ["java", "-jar", "/opt/rtree/app.jar"]