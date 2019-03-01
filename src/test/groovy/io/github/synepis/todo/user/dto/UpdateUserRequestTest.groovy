package io.github.synepis.todo.user.dto

import io.github.synepis.todo.user.UserRole
import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.Validation

class UpdateUserRequestTest extends Specification {

    def "given correct values validation should pass"() {
        given:
        def validator = Validation.buildDefaultValidatorFactory().getValidator()
        def updateUserRequest = new UpdateUserRequest(username, password, email, roles)

        when:
        def violations = validator.validate(updateUserRequest)

        then:
        violations.isEmpty()

        where:
        username | password    | email             | roles
        "user1"  | "password1" | "email@email.com" | Set.of(UserRole.ROLE_USER)
        "user1"  | "password1" | null              | Set.of(UserRole.ROLE_USER)
        "user1"  | null        | "email@email.com" | Set.of(UserRole.ROLE_USER)
        null     | "password1" | "email@email.com" | Set.of(UserRole.ROLE_USER)
        "user1"  | "password1" | "email@email.com" | null
        null     | null        | null              | null
    }

    @Unroll
    def "negative cases: when #testCaseName validation should fail"() {
        given:
        def validator = Validation.buildDefaultValidatorFactory().getValidator()
        def updateUserRequest = new UpdateUserRequest(username, password, email, Set.of())

        when:
        def violations = validator.validate(updateUserRequest)

        then:
        !violations.isEmpty()

        where:
        testCaseName                      | username | email                   | password
        "username is empty"               | ""       | "user@email.com"        | "password"
        "username is blank"               | "      " | "user@email.com"        | "password"
        "username is too short (<3)"      | "aa"     | "user@email.com"        | "password"
        "username contains illegal chars" | "aa   "  | "user@email.com"        | "password"
        "username is too long (>50)"      | "a" * 51 | "user@email.com"        | "password"
        "username has illegal characters" | "a" * 51 | "user@email.com"        | "password"

        "email empty"                     | "user"   | ""                      | "password"
        "email blank"                     | "user"   | "   "                   | "password"
        "email not email format"          | "user"   | "not-an-email"          | "password"
        "email too long (>50)"            | "user"   | "a" * 80 + "@email.com" | "password"

        "password empty"                  | "user"   | "user@email.com"        | ""
        "password too short (<8)"         | "user"   | "user@email.com"        | "1234567"
        "password too long (>50)"         | "user"   | "user@email.com"        | "a" * 51
    }
}
