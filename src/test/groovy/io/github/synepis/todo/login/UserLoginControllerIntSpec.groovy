package io.github.synepis.todo.login

import io.github.synepis.todo.IntegrationTestBaseSpec
import io.github.synepis.todo.exception.ApiError
import io.github.synepis.todo.login.dto.CreateLoginRequest
import io.github.synepis.todo.login.dto.UserLoginDto
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserLoginControllerIntSpec extends IntegrationTestBaseSpec {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private TestUserFactory testUserFactory;

    @Autowired
    private LoginUtils loginUtils;

    @Autowired
    private UserLoginRepository userLoginRepository;

    def "getAllLogins(): should work when user is admin"() {
        given:
        def adminUser = testUserFactory.anAdminUser()
        def otherUser = testUserFactory.aUser()
        def adminLogin = loginUtils.doLogin(adminUser)
        def otherLogin = loginUtils.doLogin(otherUser)
        def method = HttpMethod.GET
        def url = "/logins"

        and:
        def headers = new HttpHeaders(Map.of("x-auth-token", adminLogin.authToken))

        when:
        def response = testRestTemplate.exchange(
                url, method,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<List<UserLoginDto>>() {})

        then:
        response.statusCode == HttpStatus.OK
        response.getBody().size() == 2
        response.getBody().count { it.id == adminUser.id } == 1
        response.getBody().count { it.id == otherUser.id } == 1

        and:
        def nonAdminHeaders = new HttpHeaders(Map.of("x-auth-token", otherLogin.authToken))

        when:
        def nonAdminResponse = testRestTemplate.exchange(
                url, method,
                new HttpEntity<>(null, nonAdminHeaders), ApiError.class)

        then:
        nonAdminResponse.statusCode == HttpStatus.FORBIDDEN
        nonAdminResponse.body.status == HttpStatus.FORBIDDEN.name()
    }

    def "getAllLoginsForUser(): should work when admin or owner"() {
        given:
        def adminUser = testUserFactory.anAdminUser()
        def ownerUser = testUserFactory.aUser()
        def otherUser = testUserFactory.aUser()
        def adminLogin = loginUtils.doLogin(adminUser)
        def ownerLogin = loginUtils.doLogin(ownerUser)
        def otherLogin = loginUtils.doLogin(otherUser)
        def method = HttpMethod.GET
        def url = "/users/" + ownerUser.id + "/logins"

        and:
        def headers = new HttpHeaders(Map.of("x-auth-token", adminLogin.authToken))

        when:
        def adminResponse = testRestTemplate.exchange(
                url, method,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<List<UserLoginDto>>() {})

        then:
        adminResponse.statusCode == HttpStatus.OK
        adminResponse.getBody().size() == 1
        adminResponse.getBody().count { it.id == ownerUser.id } == 1

        and:
        def ownerHeaders = new HttpHeaders(Map.of("x-auth-token", ownerLogin.authToken))

        when:
        def ownerResponse = testRestTemplate.exchange(
                url, method,
                new HttpEntity<>(null, ownerHeaders),
                new ParameterizedTypeReference<List<UserLoginDto>>() {})

        then:
        ownerResponse.statusCode == HttpStatus.OK
        ownerResponse.getBody().size() == 1
        ownerResponse.getBody().count { it.id == ownerUser.id } == 1

        and:
        def otherHeaders = new HttpHeaders(Map.of("x-auth-token", otherLogin.authToken))

        when:
        def nonAdminResponse = testRestTemplate.exchange(
                url, method,
                new HttpEntity<>(null, otherHeaders), ApiError.class)

        then:
        nonAdminResponse.statusCode == HttpStatus.FORBIDDEN
        nonAdminResponse.body.status == HttpStatus.FORBIDDEN.name()
    }

    def "createLogin(): should authenticate the user and return a token"() {
        given:
        def user = testUserFactory.aUser()
        def createLoginRequest = new CreateLoginRequest(user.username, user.username + "password")

        when:
        def response = testRestTemplate.postForEntity("/logins", createLoginRequest, UserLoginDto.class)

        then:
        response.statusCode == HttpStatus.OK
        !response.getBody().authToken.empty
        response.getBody().userId == user.id
        response.getBody().createdOn != null
        response.getBody().expiresOn != null
    }

    def "createLogin(): user can login multiple times, every time being given a new token"() {
        given:
        def user = testUserFactory.aUser()
        def credentials = new CreateLoginRequest(user.username, user.username + "password")

        when:
        def response1 = testRestTemplate.postForEntity("/logins", credentials, UserLoginDto.class)
        def response2 = testRestTemplate.postForEntity("/logins", credentials, UserLoginDto.class)

        then:
        response1.statusCode == HttpStatus.OK
        response2.statusCode == HttpStatus.OK
        !response1.getBody().authToken.empty
        !response2.getBody().authToken.empty
        response1.getBody().authToken != response2.getBody().authToken
        userLoginRepository.findAll().count { it.userId == user.id } == 2
    }

    def "createLogin(): given bad credentials returns an authentication error"() {
        given:
        def credentials = new CreateLoginRequest("wrong", "credentials")

        when:
        def response = testRestTemplate.exchange(
                "/logins", HttpMethod.POST, new HttpEntity<>(credentials), ApiError.class)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED
        response.body.status == HttpStatus.UNAUTHORIZED.name()
        response.body.userMessages == ["Bad credentials were provided"]
    }

    def "createLogin(): given empty credentials returns a validation error"() {
        given:
        def emptyCredentials = new CreateLoginRequest("", "")

        when:
        def response = testRestTemplate.exchange(
                "/logins", HttpMethod.POST, new HttpEntity<>(emptyCredentials), ApiError.class)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
        response.body.status == HttpStatus.BAD_REQUEST.name()
        response.body.userMessages.contains("username: must not be empty")
        response.body.userMessages.contains("password: must not be empty")
    }

    def "deleteAllLoginsForUser(): should work when user is admin or owner"() {
        given:
        def user = testUserFactory.anAdminUser()
        def method = HttpMethod.DELETE
        def url = "/users/" + user.id + "/logins"

        and:
        def adminUser = testUserFactory.anAdminUser()
        def adminLogin = loginUtils.doLogin(adminUser)
        def adminHeaders = new HttpHeaders(Map.of("x-auth-token", adminLogin.authToken))

        when:
        loginUtils.doLogin(user)
        def adminResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(null, adminHeaders), Void.class)

        then:
        adminResponse.statusCode == HttpStatus.OK
        userLoginRepository.findAll().count { it.userId == user.id } == 0

        and:
        def ownerUser = user
        def ownerLogin = loginUtils.doLogin(ownerUser)
        def ownerHeaders = new HttpHeaders(Map.of("x-auth-token", ownerLogin.authToken))

        when:
        loginUtils.doLogin(user)
        def ownerResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(null, ownerHeaders), Void.class)

        then:
        ownerResponse.statusCode == HttpStatus.OK
        userLoginRepository.findAll().count { it.userId == user.id } == 0

        and:
        def otherUser = testUserFactory.aUser()
        def otherLogin = loginUtils.doLogin(otherUser)
        def otherHeaders = new HttpHeaders(Map.of("x-auth-token", otherLogin.authToken))

        when:
        loginUtils.doLogin(user)
        def nonAdminResponse = testRestTemplate.exchange(
                url, method,
                new HttpEntity<>(null, otherHeaders), ApiError.class)

        then:
        nonAdminResponse.statusCode == HttpStatus.FORBIDDEN
        userLoginRepository.findAll().count { it.userId == user.id } == 1
    }

    def "deleteLoginForUser(): should work when user is admin or owner"() {
        given:
        def user = testUserFactory.anAdminUser()
        def method = HttpMethod.DELETE
        def userLogin1 = loginUtils.doLogin(user)

        and:
        def adminUser = testUserFactory.anAdminUser()
        def adminLogin = loginUtils.doLogin(adminUser)
        def adminHeaders = new HttpHeaders(Map.of("x-auth-token", adminLogin.authToken))

        when:
        def userLogin2 = loginUtils.doLogin(user)
        def url = "/users/" + user.id + "/logins/" + userLogin2.id
        def adminResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(null, adminHeaders), Void.class)

        then:
        adminResponse.statusCode == HttpStatus.OK
        userLoginRepository.findAll().count { it.userId == user.id && it.authToken == userLogin1.authToken } == 1

        and:
        def ownerUser = user
        def ownerLogin = loginUtils.doLogin(ownerUser)
        def ownerHeaders = new HttpHeaders(Map.of("x-auth-token", ownerLogin.authToken))

        when:
        userLogin2 = loginUtils.doLogin(user)
        url = "/users/" + user.id + "/logins/" + userLogin2.id
        def ownerResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(null, ownerHeaders), Void.class)

        then:
        ownerResponse.statusCode == HttpStatus.OK
        userLoginRepository.findAll().count { it.userId == user.id && it.authToken == userLogin1.authToken } == 1

        and:
        def otherUser = testUserFactory.aUser()
        def otherLogin = loginUtils.doLogin(otherUser)
        def otherHeaders = new HttpHeaders(Map.of("x-auth-token", otherLogin.authToken))

        when:
        userLogin2 = loginUtils.doLogin(user)
        url = "/users/" + user.id + "/logins/" + userLogin2.id
        def nonAdminResponse = testRestTemplate.exchange(
                url, method, new HttpEntity<>(null, otherHeaders), ApiError.class)

        then:
        nonAdminResponse.statusCode == HttpStatus.FORBIDDEN
        userLoginRepository.findAll().count { it.userId == user.id && it.authToken == userLogin1.authToken } == 1
    }
}
