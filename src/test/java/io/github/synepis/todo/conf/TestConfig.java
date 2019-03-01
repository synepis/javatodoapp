package io.github.synepis.todo.conf;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class TestConfig {

    @Bean(initMethod = "start")
    @Primary
    public PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer("postgres:9.4");
    }

    @Bean
    @Primary
    public DataSource dataSource(PostgreSQLContainer container) {

        log.info("Connecting to test container {}:{}@{}",
                container.getJdbcUrl(), container.getUsername(), container.getPassword());

        var dataSource = DataSourceBuilder.create()
                .url(container.getJdbcUrl())
                .username(container.getUsername())
                .password(container.getPassword())
                .build();

        var flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.migrate();

        return dataSource;
    }

    @Bean
    @Primary
    public PasswordEncoder devPasswordEncoder() {
        /* Full strength BCrypt is slow, we weaken it during tests for a small speed up of tests*/
        return new BCryptPasswordEncoder(4);
    }
}
