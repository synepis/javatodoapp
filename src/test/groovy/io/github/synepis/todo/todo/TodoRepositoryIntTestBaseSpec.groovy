package io.github.synepis.todo.todo

import io.github.synepis.todo.IntegrationTestBaseSpec
import io.github.synepis.todo.user.User
import io.github.synepis.todo.utils.TestUserFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TodoRepositoryIntTestBaseSpec extends IntegrationTestBaseSpec {

    private User user
    private User user2

    @Autowired
    private TodoRepository todoRepository

    @Autowired
    private TestUserFactory testUserFactory;


    def setup() {
        user = testUserFactory.aUser()
        user2 = testUserFactory.aUser()
    }

    def "when no todos are present the count should be zero"() {
        expect:
        todoRepository.count() == 0
    }

    def "can add a todo for a user and retrieve it"() {
        when:
        def todo = todoRepository.insert(aTodo("todo1", user.id))
        def retrieved = todoRepository.findById(todo.id).get()
        def userCount = todoRepository.count()

        then:
        todo == retrieved
        userCount == 1
    }

    def "able to update a todo"() {
        given:
        def todo = todoRepository.insert(aTodo("todo1", user.id))

        when:
        todo.description = "new description"
        def success = todoRepository.update(todo)
        def retrieved = todoRepository.findById(todo.id).get()

        then:
        success
        todo == retrieved
    }

    def "able to delete a todo"() {
        given:
        def todo = todoRepository.insert(aTodo("todo1", user.id))

        when:
        def success = todoRepository.delete(todo)
        def retrieved = todoRepository.findById(todo.id)

        then:
        success
        retrieved.isEmpty()
    }

    def "cannot update a non-existent todo"() {
        when:
        def success = todoRepository.update(aTodo("todo1", user.id))

        then:
        !success
    }

    def "cannot delete a non-existent todo"() {
        when:
        def success = todoRepository.delete(aTodo("todo1", user.id).withId(0))

        then:
        !success
    }

    def "can add multiple todos for a user and retrieve them"() {
        when:
        def todo1 = todoRepository.insert(aTodo("todo1", user.id))
        def todo2 = todoRepository.insert(aTodo("todo2", user.id))
        def todo3 = todoRepository.insert(aTodo("todo3", user.id))
        def todo4 = todoRepository.insert(aTodo("todo3", user2.id))
        def retrieved1 = todoRepository.findByUserId(user.id)
        def retrieved2 = todoRepository.findByUserId(user2.id)

        then:
        todo1 == retrieved1.get(0)
        todo2 == retrieved1.get(1)
        todo3 == retrieved1.get(2)
        todo4 == retrieved2.get(0)
    }

    def aTodo(String title, Long userId) {
        return Todo.builder()
                .userId(userId)
                .title(title)
                .description("description")
                .createdOn(Instant.now())
                .priority(TodoPriority.MEDIUM)
                .status(TodoStatus.NOT_STARTED)
                .build()
    }

    def aUser(String username) {
        return User.builder()
                .username(username)
                .email(username + "@email.com")
                .password(username + "pass1")
                .createdOn(Instant.now())
                .build()
    }


}
