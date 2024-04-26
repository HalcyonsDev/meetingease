FROM openjdk:17-jdk

WORKDIR /app

COPY build/libs/meetingease-0.0.1-SNAPSHOT.jar /app/meetingease.jar

CMD ["java", "-jar", "meetingease.jar"]