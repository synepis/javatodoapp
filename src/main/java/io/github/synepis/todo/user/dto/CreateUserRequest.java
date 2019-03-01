package io.github.synepis.todo.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.github.synepis.todo.validation.ValidEmail;
import io.github.synepis.todo.validation.ValidPassword;
import io.github.synepis.todo.validation.ValidUsername;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = CreateUserRequest.CreateUserRequestBuilder.class)
public class CreateUserRequest {

    @ValidUsername
    private String username;

    @ValidPassword
    private String password;

    @ValidEmail
    private String email;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CreateUserRequestBuilder {
    }
}
