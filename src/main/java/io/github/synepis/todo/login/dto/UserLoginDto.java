package io.github.synepis.todo.login.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.github.synepis.todo.login.UserLogin;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Value
@Builder
@JsonDeserialize(builder = UserLoginDto.UserLoginDtoBuilder.class)
public class UserLoginDto {

    @NotNull
    private long id;

    @NotNull
    private long userId;

    @NotNull
    private String authToken;

    @NotNull
    private Instant createdOn;

    @NotNull
    private Instant expiresOn;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserLoginDtoBuilder {
    }

    public static UserLoginDto map(UserLogin userLogin) {
        return UserLoginDto.builder()
                .id(userLogin.getId())
                .userId(userLogin.getUserId())
                .authToken(userLogin.getAuthToken())
                .createdOn(userLogin.getCreatedOn())
                .expiresOn(userLogin.getExpiresOn())
                .build();
    }
}
