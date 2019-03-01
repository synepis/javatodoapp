package io.github.synepis.todo.todo

import io.github.synepis.todo.IntegrationTestBaseSpec
import io.github.synepis.todo.exception.ApiError
import io.github.synepis.todo.todo.dto.CreateTodoRequest
import io.github.synepis.todo.todo.dto.TodoDto
import io.github.synepis.todo.todo.dto.UpdateTodoRequest
import io.github.synepis.todo.utils.LoginUtils
import io.github.synepis.todo.utils.TestTodoFactory
import io.github.synepis.todo.utils.TestUserFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TodoControllerIntSpec extends IntegrationTestBaseSpec {

    @Autowired
    private TestRestTemplate testRestTemplate

    @Autowired
    private TestUserFactory testUserFactory

    @Autowired
    private TestTodoFactory testTodoFactory

    @Autowired
    private LoginUtils loginUtils

    @Autowired
    private TodoRepository todoRepository

    def "getAllTodo(): should work when user is admin"() {
        given:
        def user = testUserFactory.aUser()
        def todos = [
                testTodoFactory.aTodo(user),
                testTodoFactory.aTodo(user),
                testTodoFactory.aTodo(user)
        ]
        def method = HttpMethod.GET
        def url = "/todos"

        and:
        def adminUser = testUserFactory.anAdminUser()
        def adminLogin = loginUtils.doLogin(adminUser)
        def adminHeaders = new HttpHeaders(Map.of("x-auth-token", adminLogin.authToken))

        when:
        def response = testRestTemplate.exchange(
                url, method,
                new HttpEntity<>(null, adminHeaders),
                new ParameterizedTypeReference<List<TodoDto>>() {})

        then:
        response.statusCode == HttpStatus.OK
        response.getBody().size() == 3
        response.getBody().count { it.id == todos[0].id } == 1
        response.getBody().count { it.id == todos[1].id } == 1
        response.getBody().count { it.id == todos[2].id } == 1

        and:
        def otherUser = testUserFactory.aUser()
        def otherLogin = loginUtils.doLogin(otherUser)
        def nonAdminHeaders = new HttpHeaders(Map.of("x-auth-token", otherLogin.authToken))

        when:
        def nonAdminResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(null, nonAdminHeaders), ApiError.class)

        then:
        nonAdminResponse.statusCode == HttpStatus.FORBIDDEN
        nonAdminResponse.body.status == HttpStatus.FORBIDDEN.name()
    }

    def "getTodo(): should work when user is admin"() {
        given:
        def user = testUserFactory.aUser()
        def todo = testTodoFactory.aTodo(user)
        def method = HttpMethod.GET
        def url = "/todos/" + todo.id

        and:
        def adminUser = testUserFactory.anAdminUser()
        def adminLogin = loginUtils.doLogin(adminUser)
        def adminHeaders = new HttpHeaders(Map.of("x-auth-token", adminLogin.authToken))

        when:
        def response = testRestTemplate.exchange(
                url, method, new HttpEntity<>(null, adminHeaders), TodoDto.class)

        then:
        response.statusCode == HttpStatus.OK
        response.getBody().id == todo.id

        and:
        def otherUser = testUserFactory.aUser()
        def otherLogin = loginUtils.doLogin(otherUser)
        def otherHeaders = new HttpHeaders(Map.of("x-auth-token", otherLogin.authToken))

        when:
        def nonAdminResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(null, otherHeaders), ApiError.class)

        then:
        nonAdminResponse.statusCode == HttpStatus.FORBIDDEN
        nonAdminResponse.body.status == HttpStatus.FORBIDDEN.name()
    }

    def "createTodoForUser(): should work when user is admin"() {
        given:
        def user = testUserFactory.aUser()
        def method = HttpMethod.POST
        def url = "/users/" + user.id + "/todos"
        def createTodoRequest = new CreateTodoRequest(
                "title1", "description1", TodoPriority.MEDIUM, TodoStatus.IN_PROGRESS)

        and:
        def adminUser = testUserFactory.anAdminUser()
        def adminLogin = loginUtils.doLogin(adminUser)
        def adminHeaders = new HttpHeaders()
        adminHeaders.set("x-auth-token", adminLogin.authToken)

        when:
        def adminResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(createTodoRequest, adminHeaders), TodoDto.class)

        then:
        adminResponse.statusCode == HttpStatus.OK
        adminResponse.body.userId == user.id
        adminResponse.body.title == "title1"
        adminResponse.body.description == "description1"
        adminResponse.body.priority == TodoPriority.MEDIUM
        adminResponse.body.status == TodoStatus.IN_PROGRESS
        todoRepository.findByIdAndUserId(adminResponse.body.id, adminResponse.body.userId).isPresent()
    }

    def "createTodoForUser(): should work when user is owner"() {
        given:
        def user = testUserFactory.aUser()
        def method = HttpMethod.POST
        def url = "/users/" + user.id + "/todos"
        def createTodoRequest = new CreateTodoRequest(
                "title1", "description1", TodoPriority.MEDIUM, TodoStatus.IN_PROGRESS)
        and:
        def ownerUser = user
        def ownerLogin = loginUtils.doLogin(ownerUser)
        def ownerHeaders = new HttpHeaders()
        ownerHeaders.set("x-auth-token", ownerLogin.authToken)

        when:
        def ownerResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(createTodoRequest, ownerHeaders), TodoDto.class)

        then:
        ownerResponse.statusCode == HttpStatus.OK
        ownerResponse.body.userId == user.id
        ownerResponse.body.title == "title1"
        ownerResponse.body.description == "description1"
        ownerResponse.body.priority == TodoPriority.MEDIUM
        ownerResponse.body.status == TodoStatus.IN_PROGRESS
        todoRepository.findByIdAndUserId(ownerResponse.body.id, ownerResponse.body.userId).isPresent()
    }

    def "createTodoForUser(): should fail  when user is not admin or owner"() {
        given:
        def user = testUserFactory.aUser()
        def method = HttpMethod.POST
        def url = "/users/" + user.id + "/todos"
        def createTodoRequest = new CreateTodoRequest(
                "title1", "description1", TodoPriority.MEDIUM, TodoStatus.IN_PROGRESS)

        and:
        def otherUser = testUserFactory.aUser()
        def otherLogin = loginUtils.doLogin(otherUser)
        def otherHeaders = new HttpHeaders()
        otherHeaders.set("x-auth-token", otherLogin.authToken)

        when:
        def otherResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(createTodoRequest, otherHeaders), ApiError.class)

        then:
        otherResponse.statusCode == HttpStatus.FORBIDDEN
        todoRepository.findAll().size() == 0

        when:
        def noAuthResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(createTodoRequest, null), ApiError.class)

        then:
        noAuthResponse.statusCode == HttpStatus.UNAUTHORIZED
        todoRepository.findAll().size() == 0
    }

    def "updateTodoForUser(): should work when user is admin"() {
        given:
        def user = testUserFactory.aUser()
        def todo = testTodoFactory.aTodo(user)
        def method = HttpMethod.PUT
        def url = "/users/" + user.id + "/todos/" + todo.id
        def updateTodoRequest = UpdateTodoRequest.builder()
                .title("new title")
                .build()
        and:
        def adminUser = testUserFactory.anAdminUser()
        def adminLogin = loginUtils.doLogin(adminUser)
        def adminHeaders = new HttpHeaders()
        adminHeaders.set("x-auth-token", adminLogin.authToken)

        when:
        def adminResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(updateTodoRequest, adminHeaders), TodoDto.class)

        then:
        adminResponse.statusCode == HttpStatus.OK
        todoRepository.findAll().count { it.id == todo.id && it.title == "new title" } == 1
    }

    def "updateTodoForUser(): should work when user is owner"() {
        given:
        def user = testUserFactory.aUser()
        def todo = testTodoFactory.aTodo(user)
        def method = HttpMethod.PUT
        def url = "/users/" + user.id + "/todos/" + todo.id
        def updateTodoRequest = UpdateTodoRequest.builder()
                .title("new title")
                .build()
        and:
        def ownerUser = user
        def ownerLogin = loginUtils.doLogin(ownerUser)
        def ownerHeaders = new HttpHeaders()
        ownerHeaders.set("x-auth-token", ownerLogin.authToken)

        when:
        def ownerResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(updateTodoRequest, ownerHeaders), TodoDto.class)

        then:
        ownerResponse.statusCode == HttpStatus.OK
        todoRepository.findAll().count { it.id == todo.id && it.title == "new title" } == 1
    }

    def "updateTodoForUser(): should faile when user is not admin or owner"() {
        given:
        def user = testUserFactory.aUser()
        def todo = testTodoFactory.aTodo(user)
        def method = HttpMethod.PUT
        def url = "/users/" + user.id + "/todos/" + todo.id
        def updateTodoRequest = UpdateTodoRequest.builder()
                .title("new title")
                .build()
        and:
        def otherUser = testUserFactory.aUser()
        def otherLogin = loginUtils.doLogin(otherUser)
        def otherHeaders = new HttpHeaders()
        otherHeaders.set("x-auth-token", otherLogin.authToken)

        when:
        def otherResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(updateTodoRequest, otherHeaders), ApiError.class)

        then:
        otherResponse.statusCode == HttpStatus.FORBIDDEN
        todoRepository.findAll().count { it.id == todo.id && it.title == todo.title } == 1

        when:
        def noAuthResponse= testRestTemplate.exchange(
                url, method, new HttpEntity<>(updateTodoRequest, null), ApiError.class)

        then:
        noAuthResponse.statusCode == HttpStatus.UNAUTHORIZED
        todoRepository.findAll().count { it.id == todo.id && it.title == todo.title } == 1
    }

    def "deleteTodoForUser(): can delete when user is admin"() {
        given:
        def user = testUserFactory.aUser()
        def todo = testTodoFactory.aTodo(user)
        def method = HttpMethod.DELETE
        def url = "/users/" + user.id + "/todos/" + todo.id

        and:
        def adminUser = testUserFactory.anAdminUser()
        def adminLogin = loginUtils.doLogin(adminUser)
        def adminHeaders = new HttpHeaders()
        adminHeaders.set("x-auth-token", adminLogin.authToken)

        when:
        def adminResponse = testRestTemplate.exchange(url, method, new HttpEntity<>(null, adminHeaders), Void.class)

        then:
        adminResponse.statusCode == HttpStatus.OK
        todoRepository.findAll().count { it.id == todo.id } == 0
    }

    def "deleteTodoForUser(): can delete when user is owner"() {
        given:
        def user = testUserFactory.aUser()
        def todo = testTodoFactory.aTodo(user)
        def method = HttpMethod.DELETE
        def url = "/users/" + user.id + "/todos/" + todo.id

        and:
        def ownerUser = user
        def ownerLogin = loginUtils.doLogin(ownerUser)
        def ownerHeaders = new HttpHeaders()
        ownerHeaders.set("x-auth-token", ownerLogin.authToken)

        when:
        def ownerResponse = testRestTemplate.exchange(url, method, new HttpEntity<>(null, ownerHeaders), Void.class)

        then:
        ownerResponse.statusCode == HttpStatus.OK
        todoRepository.findAll().count { it.id == todo.id } == 0
    }

    def "deleteTodoForUser(): should fail when user is not admin or owner"() {
        given:
        def user = testUserFactory.aUser()
        def todo = testTodoFactory.aTodo(user)
        def method = HttpMethod.DELETE
        def url = "/users/" + user.id + "/todos/" + todo.id

        and:
        def otherUser = testUserFactory.aUser()
        def otherLogin = loginUtils.doLogin(otherUser)
        def otherHeaders = new HttpHeaders()
        otherHeaders.set("x-auth-token", otherLogin.authToken)

        when:
        def otherResponse = testRestTemplate.exchange(url, method, new HttpEntity<>(null, otherHeaders), Void.class)

        then:
        otherResponse.statusCode == HttpStatus.FORBIDDEN
        todoRepository.findById(todo.id).isPresent()

        when:
        def noAuthResponse = testRestTemplate.exchange(url, method, new HttpEntity<>(null, null), ApiError.class)

        then:
        noAuthResponse.statusCode == HttpStatus.UNAUTHORIZED
        todoRepository.findById(todo.id).isPresent()
    }
}
