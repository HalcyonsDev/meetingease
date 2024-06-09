FROM gradle:8.4-jdk-alpine as build

WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN gradle build -x test --no-daemon

FROM openjdk:17-jdk
WORKDIR /app

COPY --from=build /workspace/app/build/libs/meetingease-0.0.1-SNAPSHOT.jar /app/meetingease.jar
COPY .env /app/.env

CMD ["java", "-jar", "/app/meetingease.jar"]