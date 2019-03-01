package io.github.synepis.todo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

abstract class IntegrationTestBaseSpec extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    // Runs before every integration test, giving us a clean slate
    def cleanup() {
        def tables = [ 'public."user"', 'todo' ]

        tables.forEach { jdbcTemplate.execute("TRUNCATE " + it + " CASCADE") }
    }
}
