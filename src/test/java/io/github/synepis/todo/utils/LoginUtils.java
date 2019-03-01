package io.github.synepis.todo.utils;

import io.github.synepis.todo.login.dto.CreateLoginRequest;
import io.github.synepis.todo.login.dto.UserLoginDto;
import io.github.synepis.todo.user.User;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class LoginUtils {

    private TestRestTemplate testRestTemplate;

    public LoginUtils(TestRestTemplate testRestTemplate) {
        this.testRestTemplate = testRestTemplate;
    }

    public UserLoginDto doLogin(User user) {
        return testRestTemplate
                .postForEntity(
                        "/logins",
                        new CreateLoginRequest(user.getUsername(), user.getUsername() + "password"),
                        UserLoginDto.class
                ).getBody();
    }

    public boolean doLogout(User user, String authToken) {
        var headers = new HttpHeaders();
        headers.set("x-auth-token", authToken);

        var response = testRestTemplate.exchange(
            "/logins", HttpMethod.DELETE, new HttpEntity<Void>(null, headers), Void.class);

        return response.getStatusCode() == HttpStatus.OK;
    }
}
