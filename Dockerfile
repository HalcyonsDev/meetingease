FROM openjdk:20-jdk

WORKDIR /app

COPY build/libs/meetingease-0.0.1-SNAPSHOT-plain.jar /app/meetingease.jar

CMD ["java", "-jar", "meetingease.jar"]