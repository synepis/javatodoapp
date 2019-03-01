package io.github.synepis.todo.user

import io.github.synepis.todo.IntegrationTestBaseSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DuplicateKeyException

import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserRepositoryIntSpec extends IntegrationTestBaseSpec {

    @Autowired
    private UserRepository userRepository

    def "when no users are present the count should be zero"() {
        expect:
        userRepository.count() == 0
    }

    def "able to add and retrieve a new user"() {
        when:
        def user = userRepository.insert(aUser("user"))
        def retrieved = userRepository.findById(user.getId()).get()
        def userCount = userRepository.count()

        then:
        user == retrieved
        userCount == 1
    }

    def "able to update a user"() {
        given:
        def user = userRepository.insert(aUser("user1"))
        user = user.withLastLoginOn(Instant.now())

        when:
        def success = userRepository.update(user)
        def retrieved = userRepository.findById(user.getId()).get()

        then:
        success
        user == retrieved
    }

    def "able to delete a user"() {
        given:
        def user = userRepository.insert(aUser("user1"))

        when:
        def success = userRepository.delete(user)
        def retrieved = userRepository.findById(user.getId())

        then:
        success
        retrieved.isEmpty()
    }


    def "cannot two users with the same name"() {
        when:
        userRepository.insert(aUser("user1"))
        userRepository.insert(aUser("user1"))

        then:
        thrown DuplicateKeyException
    }

    def "cannot update a non-existent users"() {
        when:
        def success = userRepository.update(aUser("user1").withId(1))

        then:
        !success
    }

    def "cannot delete a non-existent users"() {
        when:
        def success = userRepository.delete(aUser("user1").withId(1))

        then:
        !success
    }

    def "can add multiple users and retrieve them"() {
        when:
        def user1 = userRepository.insert(aUser("user1"))
        def user2 = userRepository.insert(aUser("user2"))
        def user3 = userRepository.insert(aUser("user3"))

        def retrieved = userRepository.findAll()

        then:
        user1 == retrieved.get(0)
        user2 == retrieved.get(1)
        user3 == retrieved.get(2)
    }

    def aUser(String username) {
        return User.builder()
                .username(username)
                .email(username + "@email.com")
                .password(username + "pass1")
                .createdOn(Instant.now())
                .roles(Set.of(UserRole.ROLE_USER))
                .build()
    }
}
