# Project Documentation

## Instruction for running the application

To run the application, follow these steps:

1. Install Docker and Docker Compose on your computer.
2. Download or clone the repository with the project.
3. Open the terminal in the root directory of the project
4. Run the command `docker compose up` to build and run containers

The docker-compose.yaml file is located in the root directory of the project that describes the configuration of the application. It includes:

- A container with a PostgreSQL database.
- A container with an application in the Java language

## About project

The application is a service for arranging meetings of legal entities with company agents. It simplifies all the difficulties and solves many problems with making meetings.

The main functions of the application:

1. The client can have the option to select the date/time and venue of the meeting.
2. The client can receive information on the documents that he needs to prepare for the meeting.
3. The client can know who is coming to him with full information
4. The ability to register a company and assign several clients to a meeting
5. Agents travel to different addresses at different times, so their availability is taken into account
6. Chat for communication between the client and the agent
7. Client verification by mail
8. Possibility to change meeting statuses

## Description of dependencies

This project has many dependencies. You can find more information about them in the [dependency document](dependencies.md)

