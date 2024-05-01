package ru.halcyon.meetingease;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestPostgresContainer extends PostgreSQLContainer<TestPostgresContainer> {
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.6");

    public TestPostgresContainer(String name) {
        super(name);
    }

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    public static PostgreSQLContainer<?> getInstance() {
        return postgres;
    }
}
