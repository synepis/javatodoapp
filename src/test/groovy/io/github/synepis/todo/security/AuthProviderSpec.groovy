package io.github.synepis.todo.security

import io.github.synepis.todo.login.UserLogin
import io.github.synepis.todo.login.UserLoginRepository
import io.github.synepis.todo.user.User
import io.github.synepis.todo.user.UserRepository
import io.github.synepis.todo.user.UserRole
import org.springframework.security.authentication.BadCredentialsException
import spock.lang.Specification

import java.time.Clock
import java.time.Instant

class AuthProviderSpec extends Specification {

    private AuthProvider authProvider
    private def clock = Mock(Clock)
    private def userRepository = Mock(UserRepository)
    private def userLoginRepository = Mock(UserLoginRepository)

    def setup() {
        authProvider = new AuthProvider(clock, userRepository, userLoginRepository)
    }

    def "given a valid token returns a valid authentication"() {
        given:
        def tokenContainer = new AuthTokenContainer("token-value")
        def createdOn = Instant.now()
        def expiresOn = createdOn.plusSeconds(10)
        def userLogin = new UserLogin(1, 1, "token-value", createdOn, expiresOn)
        def user = User.builder()
                .id(1)
                .username("user1")
                .password("password1")
                .email("user1@email.com")
                .createdOn(Instant.now())
                .lastLoginOn(Instant.now())
                .roles(Set.of(UserRole.ROLE_USER, UserRole.ROLE_ADMIN))
                .build()
        when:
        def authentication = authProvider.authenticate(tokenContainer)

        then:
        1 * userLoginRepository.findByAuthToken(tokenContainer.token) >> Optional.of(userLogin)
        1 * clock.instant() >> createdOn
        1 * userRepository.findById(userLogin.userId) >> Optional.of(user)
        authentication.authenticated
        authentication.authorities.size() == 2
        authentication.authorities.count { it.getAuthority() == UserRole.ROLE_ADMIN.name() } == 1
        authentication.authorities.count { it.getAuthority() == UserRole.ROLE_USER.name() } == 1
    }

    def "given a unrecognized token throw exception"() {
        given:
        def tokenContainer = new AuthTokenContainer(null)

        when:
        authProvider.authenticate(tokenContainer)

        then:
        1 * userLoginRepository.findByAuthToken(tokenContainer.token) >> Optional.empty()
        thrown BadCredentialsException
    }

    def "given an expired token throw exception"() {
        given:
        def tokenContainer = new AuthTokenContainer("token-value")
        def createdOn = Instant.now()
        def expiresOn = createdOn.plusSeconds(10)
        def userLogin = new UserLogin(1, 1, "token-value", createdOn, expiresOn)

        when:
        authProvider.authenticate(tokenContainer)

        then:
        1 * userLoginRepository.findByAuthToken(tokenContainer.token) >> Optional.of(userLogin)
        1 * clock.instant() >> expiresOn.plusSeconds(1)
        thrown BadCredentialsException
    }

    def "given a token with no corresponding user throw exception"() {
        given:
        def tokenContainer = new AuthTokenContainer("token-value")
        def createdOn = Instant.now()
        def expiresOn = createdOn.plusSeconds(10)
        def userLogin = new UserLogin(1, 1, "token-value", createdOn, expiresOn)

        when:
        authProvider.authenticate(tokenContainer)

        then:
        1 * userLoginRepository.findByAuthToken(tokenContainer.token) >> Optional.of(userLogin)
        1 * clock.instant() >> createdOn
        1 * userRepository.findById(userLogin.userId) >> Optional.empty()
        thrown BadCredentialsException
    }

    def "supports AuthTokenContainer class"() {
        when:
        def supportsAuthTokenContainer = authProvider.supports(AuthTokenContainer.class)
        def supportOtherTypes = authProvider.supports(Object.class)

        then:
        supportsAuthTokenContainer
        !supportOtherTypes
    }
}
