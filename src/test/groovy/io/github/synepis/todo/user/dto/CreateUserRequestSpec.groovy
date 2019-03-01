package io.github.synepis.todo.user.dto


import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.Validation

class CreateUserRequestSpec extends Specification {

    def "given correct values validation should pass"() {
        given:
        def validator = Validation.buildDefaultValidatorFactory().getValidator()
        def createUserRequest = new CreateUserRequest("user", "password", "user@email.com")

        when:
        def violations = validator.validate(createUserRequest)

        then:
        violations.isEmpty()
    }

    @Unroll
    def "when #testCaseName validation should fail"() {
        given:
        def validator = Validation.buildDefaultValidatorFactory().getValidator()
        def createUserRequest = new CreateUserRequest(username, password, email)

        when:
        def violations = validator.validate(createUserRequest)

        then:
        !violations.isEmpty()

        where:
        testCaseName                      | username | password   | email
        "username is null"                | null     | "password" | "user@email.com"
        "username is empty"               | ""       | "password" | "user@email.com"
        "username is blank"               | "      " | "password" | "user@email.com"
        "username is too short (<3)"      | "aa"     | "password" | "user@email.com"
        "username contains illegal chars" | "aa   "  | "password" | "user@email.com"
        "username is too long (>50)"      | "a" * 51 | "password" | "user@email.com"
        "username has illegal characters" | "a" * 51 | "password" | "user@email.com"
        "email null"                      | "user"   | "password" | null
        "email empty"                     | "user"   | "password" | ""
        "email blank"                     | "user"   | "password" | "   "
        "email not email format"          | "user"   | "password" | "not-an-email"
        "email too long (>50)"            | "user"   | "password" | "a" * 80 + "@email.com"
        "password null"                   | "user"   | null       | "user@email.com"
        "password empty"                  | "user"   | ""         | "user@email.com"
        "password too short (<8)"         | "user"   | "1234567"  | "user@email.com"
        "password too long (>50)"         | "user"   | "a" * 51   | "user@email.com"
    }
}
