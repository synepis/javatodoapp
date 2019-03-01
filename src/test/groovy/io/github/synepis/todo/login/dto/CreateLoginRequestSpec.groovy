package io.github.synepis.todo.login.dto


import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.Validation

class CreateLoginRequestSpec extends Specification {

    @Unroll
    def "test validation: given '#username', '#password' it should have #expectedViolations violations"() {
        given:
        def validator = Validation.buildDefaultValidatorFactory().getValidator()
        def createLoginRequest = new CreateLoginRequest(username, password)

        when:
        def violations = validator.validate(createLoginRequest)

        then:
        violations.size() == expectedViolations

        where:
        username   | password   | expectedViolations
        null       | null       | 2
        null       | "password" | 1
        ""         | "password" | 1
        "username" | null       | 1
        "username" | ""         | 1
        "username" | "password" | 0
    }
}
