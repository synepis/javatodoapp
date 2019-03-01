package io.github.synepis.todo.user

import io.github.synepis.todo.IntegrationTestBaseSpec
import io.github.synepis.todo.exception.ApiError
import io.github.synepis.todo.user.dto.CreateUserRequest
import io.github.synepis.todo.user.dto.UpdateUserRequest
import io.github.synepis.todo.user.dto.UserDto
import io.github.synepis.todo.utils.TestUserFactory
import io.github.synepis.todo.utils.LoginUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Unroll

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntSpec extends IntegrationTestBaseSpec {

    @Autowired
    private TestRestTemplate testRestTemplate

    @Autowired
    private TestUserFactory testUserFactory

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginUtils loginUtils

    @Autowired
    private PasswordEncoder passwordEncoder

    def "getAllUsers(): should work when admin otherwise forbidden"() {
        given:
        def user = testUserFactory.anAdminUser()
        def otherUser = testUserFactory.aUser()
        def userLogin = loginUtils.doLogin(user)
        def headers = new HttpHeaders(Map.of("x-auth-token", userLogin.authToken))

        when:
        def response = testRestTemplate.exchange(
                "/users", HttpMethod.GET,
                new HttpEntity<Void>(null, headers),
                new ParameterizedTypeReference<List<UserDto>>() {})

        then:
        response.statusCode == HttpStatus.OK
        response.getBody().size() == 2
        response.getBody().count { it.id == user.id } == 1
        response.getBody().count { it.id == otherUser.id } == 1

        and:
        def nonAdminUser = testUserFactory.aUser()
        def nonAdminLogin = loginUtils.doLogin(nonAdminUser)
        def nonAdminHeaders = new HttpHeaders(Map.of("x-auth-token", nonAdminLogin.authToken))

        when:
        def nonAdminResponse = testRestTemplate.exchange(
                "/users", HttpMethod.GET,
                new HttpEntity<Void>(null, nonAdminHeaders), ApiError.class)

        then:
        nonAdminResponse.statusCode == HttpStatus.FORBIDDEN
        nonAdminResponse.body.status == HttpStatus.FORBIDDEN.name()
    }

    def "getUser(): should work when admin or owner"() {
        given:
        def user = testUserFactory.anAdminUser()
        def otherUser = testUserFactory.aUser()
        def userLogin = loginUtils.doLogin(user)
        def headers = new HttpHeaders(Map.of("x-auth-token", userLogin.authToken))

        when:
        def response = testRestTemplate.exchange(
                "/users", HttpMethod.GET,
                new HttpEntity<Void>(null, headers),
                new ParameterizedTypeReference<List<UserDto>>() {})

        then:
        response.statusCode == HttpStatus.OK
        response.getBody().size() == 2
        response.getBody().count { it.id == user.id } == 1
        response.getBody().count { it.id == otherUser.id } == 1

        and:
        def owner = user
        def ownerLogin = loginUtils.doLogin(owner)
        def ownerHeaders = new HttpHeaders(Map.of("x-auth-token", ownerLogin.authToken))

        when:
        def ownerResponse = testRestTemplate.exchange(
                "/users", HttpMethod.GET,
                new HttpEntity<Void>(null, ownerHeaders),
                new ParameterizedTypeReference<List<UserDto>>() {})

        then:
        ownerResponse.statusCode == HttpStatus.OK
        ownerResponse.getBody().size() == 2
        ownerResponse.getBody().count { it.id == user.id } == 1
        ownerResponse.getBody().count { it.id == otherUser.id } == 1

        and:
        def nonAdminUser = testUserFactory.aUser()
        def nonAdminLogin = loginUtils.doLogin(nonAdminUser)
        def nonAdminHeaders = new HttpHeaders(Map.of("x-auth-token", nonAdminLogin.authToken))

        when:
        def nonAdminResponse = testRestTemplate.exchange(
                "/users", HttpMethod.GET,
                new HttpEntity<Void>(null, nonAdminHeaders), ApiError.class)

        then:
        nonAdminResponse.statusCode == HttpStatus.FORBIDDEN
        nonAdminResponse.body.status == HttpStatus.FORBIDDEN.name()
    }

    def "createUser(): should work without being authenticated"() {
        given:
        def createUserRequest = new CreateUserRequest("user1", "password1", "user1@email.com")

        when:
        def response = testRestTemplate.postForEntity("/users", createUserRequest, UserDto.class)

        then:
        response.statusCode == HttpStatus.OK
        response.getBody().id != 0
        response.getBody().username == createUserRequest.username
        response.getBody().email == createUserRequest.email
        response.getBody().createdOn != null
        response.getBody().lastLoginOn == null
    }

    def "updateUser(): can update when user is owner"() {
        given:
        def user = testUserFactory.aUser()
        def userLogin = loginUtils.doLogin(user)

        def username = updateUsername ? "new_username" : null
        def password = updatePassword ? "new_password" : null
        def email = updateEmail ? "new_email@email.com" : null

        def updateUserRequest = UpdateUserRequest.builder()
                .username(username)
                .password(password)
                .email(email)
                .build()

        def headers = new HttpHeaders()
        headers.set("x-auth-token", userLogin.authToken)

        when:
        def response = testRestTemplate.exchange(
                "/users/" + user.getId(), HttpMethod.PUT,
                new HttpEntity<UpdateUserRequest>(updateUserRequest, headers),
                Void.class)

        def updatedUser = userRepository.findById(user.id)

        then:
        response.statusCode == HttpStatus.OK
        updatedUser.isPresent()
        updatedUser.get().username == (updateUsername ? "new_username" : user.username)
        updatedUser.get().email == (updateEmail ? "new_email@email.com" : user.email)
        if (updatePassword) {
            assert passwordEncoder.matches("new_password", updatedUser.get().password)
        } else {
            assert updatedUser.get().password == user.password
        }

        where:
        updateUsername | updatePassword | updateEmail
        false          | false          | false
        false          | false          | true
        false          | true           | false
        false          | true           | true
        true           | false          | false
        true           | false          | true
        true           | true           | false
        true           | true           | true

    }

    def "updateUser(): can update roles only when user is admin"() {
        given:
        def user = testUserFactory.aUser()
        def userLogin = loginUtils.doLogin(user)
        def updateUserRequest = UpdateUserRequest.builder().roles(Set.of(UserRole.ROLE_ADMIN)).build()
        def headers = new HttpHeaders()
        headers.set("x-auth-token", userLogin.authToken)

        when:
        def ownerResponse = testRestTemplate.exchange(
                "/users/" + user.id, HttpMethod.PUT,
                new HttpEntity<UpdateUserRequest>(updateUserRequest, headers),
                ApiError.class)
        def updatedUser = userRepository.findById(user.id)

        then:
        ownerResponse.statusCode == HttpStatus.FORBIDDEN
        updatedUser.isPresent()
        updatedUser.get().roles == Set.of(UserRole.ROLE_USER)

        and:
        def adminUser = testUserFactory.anAdminUser()
        def adminLogin = loginUtils.doLogin(adminUser)
        def adminHeaders = new HttpHeaders()
        adminHeaders.set("x-auth-token", adminLogin.authToken)

        when:
        def adminResponse = testRestTemplate.exchange(
                "/users/" + user.id, HttpMethod.PUT,
                new HttpEntity<UpdateUserRequest>(updateUserRequest, adminHeaders),
                ApiError.class)
        updatedUser = userRepository.findById(user.id)

        then:
        adminResponse.statusCode == HttpStatus.OK
        updatedUser.isPresent()
        updatedUser.get().roles == Set.of(UserRole.ROLE_ADMIN)
    }

    @Unroll
    def "updateUser(): can update, given user isAdmin=#isAdmin and isOwner=#isOwner"() {
        given:
        def user = isAdmin ? testUserFactory.anAdminUser() : testUserFactory.aUser()
        def otherUser = isOwner ? user : testUserFactory.aUser()
        def userLogin = loginUtils.doLogin(user)
        def headers = new HttpHeaders()
        headers.set("x-auth-token", userLogin.authToken)

        when:
        def response = testRestTemplate.exchange(
                "/users/" + otherUser.getId(), HttpMethod.GET,
                new HttpEntity<Void>(null, headers),
                UserDto.class)

        then:
        response.statusCode == HttpStatus.OK
        response.getBody().id == otherUser.id
        response.getBody().username == otherUser.username
        response.getBody().email == otherUser.email
        response.getBody().createdOn == otherUser.createdOn
        response.getBody().lastLoginOn == otherUser.lastLoginOn

        where:
        isAdmin | isOwner
        true    | false
        true    | true
        false   | true
    }

    def "getUser() fails without permission and displays correct error message"() {
        given:
        def user = testUserFactory.aUser()
        def otherUser = testUserFactory.aUser()
        def userLogin = loginUtils.doLogin(user)
        def headers = new HttpHeaders()
        headers.set("x-auth-token", userLogin.authToken)

        when:
        def response1 = testRestTemplate.exchange(
                "/users/" + otherUser.getId(), HttpMethod.GET,
                new HttpEntity<Void>(null, headers),
                ApiError.class)

        def response2 = testRestTemplate.exchange(
                "/users/" + otherUser.getId(), HttpMethod.GET,
                new HttpEntity<Void>(null),
                ApiError.class)

        then:
        response1.statusCode == HttpStatus.FORBIDDEN
        response1.getBody().status == HttpStatus.FORBIDDEN.name()
        response1.getBody().userMessages == ["Access is denied"]

        response2.statusCode == HttpStatus.UNAUTHORIZED
        response2.getBody().status == HttpStatus.UNAUTHORIZED.name()
        response2.getBody().userMessages == ["Authentication is required"]
    }

    def "createUser(): fails when CreateUserRequest has validation errors all error messages are present when"() {
        given:
        def createUserRequest = new CreateUserRequest("", "", "not-an-email")

        when:
        def response = testRestTemplate.postForEntity("/users", createUserRequest, ApiError.class)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
        response.getBody().status == HttpStatus.BAD_REQUEST.name()
        response.getBody().userMessages.size() == 3
        response.getBody().userMessages.count { it.startsWith("username:") } == 1
        response.getBody().userMessages.count { it.startsWith("password:") } == 1
        response.getBody().userMessages.count { it.startsWith("email:") } == 1
    }

    def "updateUser(): fails when UpdateUserRequest has validation errors all error messages are present"() {
        given:
        def user = testUserFactory.aUser()
        def userLogin = loginUtils.doLogin(user)
        def headers = new HttpHeaders()
        headers.set("x-auth-token", userLogin.authToken)

        def updateUserRequest = new UpdateUserRequest("", "", "not-an-email", null)

        when:
        def response = testRestTemplate.exchange(
                "/users/" + user.id, HttpMethod.PUT,
                new HttpEntity<UpdateUserRequest>(updateUserRequest, headers),
                ApiError.class)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
        response.getBody().status == HttpStatus.BAD_REQUEST.name()
        response.getBody().userMessages.size() == 3
        response.getBody().userMessages.count { it.startsWith("username:") } == 1
        response.getBody().userMessages.count { it.startsWith("password:") } == 1
        response.getBody().userMessages.count { it.startsWith("email:") } == 1
    }

    def "deleteUser(): can delete when user is admin"() {
        given:
        def user = testUserFactory.aUser()
        def method = HttpMethod.DELETE
        def url = "/users/" + user.id

        and:
        def adminUser = testUserFactory.anAdminUser()
        def adminLogin = loginUtils.doLogin(adminUser)
        def adminHeaders = new HttpHeaders()
        adminHeaders.set("x-auth-token", adminLogin.authToken)

        when:
        def adminResponse = testRestTemplate.exchange(url, method, new HttpEntity<>(null, adminHeaders), Void.class)

        then:
        adminResponse.statusCode == HttpStatus.OK
        userRepository.findAll().count { it.id == user.id } == 0
    }

    def "deleteUser(): should fail when user is not admin"() {
        given:
        def user = testUserFactory.aUser()
        def method = HttpMethod.DELETE
        def url = "/users/" + user.id

        and:
        def otherUser = testUserFactory.aUser()
        def otherLogin = loginUtils.doLogin(otherUser)
        def otherHeaders = new HttpHeaders()
        otherHeaders.set("x-auth-token", otherLogin.authToken)

        when:
        user = testUserFactory.aUser()
        def otherResponse = testRestTemplate.exchange(url, method,new HttpEntity<>(null, otherHeaders), Void.class)

        then:
        otherResponse.statusCode == HttpStatus.FORBIDDEN
        userRepository.findAll().count { it.id == user.id } == 1
    }
}
