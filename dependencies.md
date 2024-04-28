# Dependencies

This document describes the dependencies used in the project and provides the rationale for their inclusion.

## Spring Boot Starter Data Jpa

Dependency: `implementation 'org.springframework.boot:spring-boot-starter-data-jpa'`

Spring Boot Starter Data JPA is a module provided by the Spring Boot framework that simplifies the integration of the Java Persistence API (JPA) into Spring Boot applications. It builds upon the Spring Data JPA project, which aims to reduce the amount of boilerplate code required to implement data access layers for JPA-based repositories.

## Spring Boot Starter Security

Dependency: `implementation 'org.springframework.boot:spring-boot-starter-security'`

Spring Boot Starter Security is a module provided by the Spring Boot framework that facilitates the integration of security features into Spring Boot applications. It builds upon the powerful Spring Security framework, which offers comprehensive security solutions for Java applications.

## Spring Boot Starter Validation

Dependency: `implementation 'org.springframework.boot:spring-boot-starter-validation'`

Spring Boot Starter Validation is a part of the Spring Boot framework that simplifies the integration of validation mechanisms into your application. It's built on top of the Hibernate Validator, which is a JSR-380 (Bean Validation) implementation.

## Spring Boot Starter Web

Dependency: `implementation 'org.springframework.boot:spring-boot-starter-web'`

Spring Boot Starter Web is a module provided by the Spring Boot framework that simplifies the integration of web-related functionalities into Spring Boot applications. It's designed to streamline the development of web applications by providing essential features and configurations out of the box.

## Spring Boot Starter Mail

Dependency: `implementation 'org.springframework.boot:spring-boot-starter-mail'`

Spring Boot Starter Mail is a module provided by the Spring Boot framework that simplifies the integration of email functionality into Spring Boot applications. It allows developers to send emails easily without dealing with complex configuration and setup.

## Spring Boot Starter Websocket

Dependency: `implementation 'org.springframework.boot:spring-boot-starter-websocket'`

Spring Boot Starter WebSocket is a module provided by the Spring Boot framework that simplifies the integration of WebSocket functionality into Spring Boot applications. WebSocket is a protocol that provides full-duplex communication channels over a single TCP connection, allowing real-time bidirectional communication between clients and servers. We use it for chat.

## Spring Boot Starter Test

Dependency: `testImplementation 'org.springframework.boot:spring-boot-starter-test'`

Spring Boot Starter Test is a module provided by the Spring Boot framework to simplify and streamline the testing process for Spring Boot applications. It includes a comprehensive set of tools and utilities for writing unit tests, integration tests, and end-to-end tests for Spring Boot applications.

## Spring Security Test

Dependency: `testImplementation 'org.springframework.security:spring-security-test'`

Spring Security Test is a module provided by the Spring Security framework that offers utilities and tools for testing security configurations and features in Spring Security-enabled applications. It's particularly useful for writing unit tests and integration tests to ensure that security mechanisms are properly configured and functioning as expected.

## Liquibase

Dependency: `implementation 'org.liquibase:liquibase-core'`

Liquibase is an open-source database schema change management and version control tool. It enables developers to track, manage, and apply database schema changes across different environments in a consistent and automated manner. 

## Lombok

Dependency: `compileOnly 'org.projectlombok:lombok'`

Lombok is a popular Java library that helps reduce boilerplate code in Java projects by automatically generating common code structures during compilation. It achieves this through the use of annotations, allowing us to focus more on business logic and less on routine tasks.

## Postgresql

Dependency: `runtimeOnly 'org.postgresql:postgresql'`

The Postgresql declaration is typically used in Gradle build scripts to specify a dependency that is required only at runtime, not during compilation or testing. In this case, it specifies the PostgreSQL JDBC driver dependency, which is necessary for connecting to and interacting with a PostgreSQL database at runtime.

## Testcontainers

Dependency: `testImplementation 'org.testcontainers:testcontainers:1.19.7'`

Testcontainers is a popular Java testing library that provides lightweight, throwaway instances of databases, Selenium web browsers, or anything else that can run in a Docker container, for use in automated tests.

## Testcontainers Junit Jupiter

Dependency: `testImplementation 'org.testcontainers:junit-jupiter:1.19.7'`

This dependency includes the Testcontainers module specifically designed for integration with JUnit 5 (Jupiter). It provides utilities and annotations tailored for use with JUnit 5 test classes, allowing seamless integration of Testcontainers functionality into JUnit 5 test suites.

## Testcontainers Postgresql

Dependency: `testImplementation 'org.testcontainers:postgresql:1.19.7'`

We can simplify their test setup for PostgreSQL-dependent applications. We don't need to manually configure and manage PostgreSQL instances for testing purposes

## JJWT Impl

Dependency: `implementation 'io.jsonwebtoken:jjwt-api:0.12.5'`

This dependency includes the implementation module of the jjwt library. It contains the concrete implementations of the interfaces defined in the API module.

## JJWT API

Dependency: `implementation 'io.jsonwebtoken:jjwt-api:0.12.5'`

This dependency includes only the API module of the jjwt library. It contains interfaces and abstract classes defining the contract for working with JSON Web Tokens (JWTs). This dependency is necessary for compiling and running the application code that interacts with JWTs.

## JJWT Jackson

Dependency: `runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'`

This dependency includes the Jackson module of the jjwt library. It provides integration with the Jackson JSON library for serialization and deserialization of JWTs to and from JSON format.