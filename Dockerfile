FROM openjdk:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/bookstore-0.0.1-SNAPSHOT-standalone.jar /bookstore/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/bookstore/app.jar"]
