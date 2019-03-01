package io.github.synepis.todo.security

import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthTokenFilterSpec extends Specification {

    private def authTokenFilter = new AuthTokenFilter()
    private def request = Mock(HttpServletRequest)
    private def response = Mock(HttpServletResponse)
    private def filterChain = Mock(FilterChain)
    private def securityContext = Mock(SecurityContext)

    def setup() {
        SecurityContextHolder.setContext(securityContext)
    }

    def "extracts token if present and set the authentication properly"() {
        when:
        authTokenFilter.doFilterInternal(request, response, filterChain)

        then:
        1 * request.getHeaderNames() >> Collections.enumeration(headers + [tokenHeader])
        1 * request.getHeader(tokenHeader) >> "token-value"
        1 * filterChain.doFilter(request, response)
        1 * securityContext.setAuthentication(*_) >> { args ->
            def tokenContainer = (AuthTokenContainer) args[0]
            assert tokenContainer.userId == null
            assert tokenContainer.token == "token-value"
            assert !tokenContainer.authenticated
        }

        where:
        headers                | tokenHeader
        []                     | "x-auth-token"
        []                     | "X-aUTh-tOkEN"
        ["header1", "header2"] | "x-auth-token"
        ["header1", "header2"] | "X-AUTH-TOKEN"
    }

    def "if multiple token headers are present, the first one is used"() {
        when:
        authTokenFilter.doFilterInternal(request, response, filterChain)

        then:
        1 * request.getHeaderNames() >> Collections.enumeration(["X-AUTH-TOKEN", "x-auth-token"])
        1 * request.getHeader("X-AUTH-TOKEN") >> "TOKEN-VALUE"
        0 * request.getHeader("x-auth-token")
        1 * filterChain.doFilter(request, response)
        1 * securityContext.setAuthentication(*_) >> { args ->
            def tokenContainer = (AuthTokenContainer) args[0]
            assert tokenContainer.userId == null
            assert tokenContainer.token == "TOKEN-VALUE"
            assert !tokenContainer.authenticated
        }
    }

    def "handles empty token"() {
        when:
        authTokenFilter.doFilterInternal(request, response, filterChain)

        then:
        1 * request.getHeaderNames() >> Collections.enumeration(["x-auth-token"])
        1 * request.getHeader("x-auth-token") >> ""
        1 * filterChain.doFilter(request, response)
        0 * securityContext.setAuthentication(*_)
    }

    def "handles when token is no present"() {
        when:
        authTokenFilter.doFilterInternal(request, response, filterChain)

        then:
        1 * request.getHeaderNames() >> Collections.enumeration(["header1", "header2"])
        0 * request.getHeader("x-auth-token")
        1 * filterChain.doFilter(request, response)
        0 * securityContext.setAuthentication(*_)
    }
}
