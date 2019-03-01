package io.github.synepis.todo.login.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotEmpty;

@Value
@Builder
@AllArgsConstructor
@JsonDeserialize(builder = CreateLoginRequest.CreateLoginRequestBuilder.class)
public class CreateLoginRequest {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CreateLoginRequestBuilder {
    }
}
