package io.github.synepis.todo.user

import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

import java.time.Clock
import java.time.Instant

class UserControllerSpec extends Specification {

    private Clock clock = Mock(Clock)

    private UserRepository userRepository = Mock(UserRepository)

    private PasswordEncoder passwordEncoder = Mock(PasswordEncoder)

    private UserController userController

    def setup() {
        userController = new UserController(clock, userRepository, passwordEncoder)
    }

    def "can get all users"() {
        when:
        userController.getAllUsers()

        then:
        1 * userRepository.findAll() >> [aUser(1), aUser(2), aUser(3)]
    }

    def User aUser(long userId) {
        User.builder()
                .id(userId)
                .username("user" + userId)
                .password("pass" + userId)
                .email("user" + userId + "@mail.com")
                .createdOn(Instant.now())
                .roles(Set.of(UserRole.ROLE_USER))
                .build()

    }
}
